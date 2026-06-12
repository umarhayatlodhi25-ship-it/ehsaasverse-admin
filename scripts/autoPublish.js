import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';
import { getMessaging } from 'firebase-admin/messaging';
import { createClient } from '@supabase/supabase-js';

const supabase = createClient(
  "https://jgdrdtirmuvkoznfuuog.supabase.co",
  "sb_publishable_23VXzWVp2ofyGLegPbOMBw_rElW679g"
);

// Initialize Firebase Admin with Service Account
const serviceAccountKeyStr = process.env.FIREBASE_SERVICE_ACCOUNT;
if (!serviceAccountKeyStr) {
  console.error("Missing FIREBASE_SERVICE_ACCOUNT environment variable.");
  process.exit(1);
}

let serviceAccount;
try {
  serviceAccount = JSON.parse(serviceAccountKeyStr);
} catch (e) {
  console.error("Invalid JSON in FIREBASE_SERVICE_ACCOUNT environment variable.");
  process.exit(1);
}

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();
const messaging = getMessaging();

const PUBLISH_COUNT = 2; // How many items to publish per run

async function run() {
  console.log(`Starting Auto Publish Job. Target: ${PUBLISH_COUNT} shayaris...`);
  
  try {
    // 1. Fetch pending items from daily_queue
    const queueRef = db.collection('daily_queue');
    const snapshot = await queueRef
      .where('status', '==', 'pending')
      .orderBy('createdAt', 'asc')
      .limit(PUBLISH_COUNT)
      .get();

    if (snapshot.empty) {
      console.log('Queue is empty. No shayaris to publish.');
      return;
    }

    console.log(`Found ${snapshot.size} pending items.`);
    
    let publishedCount = 0;

    // 2. Process each item
    for (const doc of snapshot.docs) {
      const item = doc.data();
      
      const payload = {
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        status: 'published',
        isDaily: true,
        publishDate: new Date().toISOString(),
        createdAt: FieldValue.serverTimestamp()
      };

      if (item.type === 'photo') {
        const { error } = await supabase.from('image_shayari').insert([{
            title: 'Untitled',
            category: item.categoryName,
            image_url: item.imageUrl,
            status: 'published'
        }]);
        if (error) {
            console.error("Error publishing photo to Supabase:", error);
            continue; // Skip marking as published if failed
        }
      } else {
        payload.content = item.content;
        await db.collection('text_shayari').add(payload);
      }

      // Mark as published in queue
      await doc.ref.update({
        status: 'published',
        publishedAt: FieldValue.serverTimestamp()
      });
      
      publishedCount++;
    }

    console.log(`Successfully published ${publishedCount} shayaris.`);

    // 3. Send Push Notification
    if (publishedCount > 0) {
      const topic = 'all'; // Standard topic for all users
      
      // Make dynamic notification text based on number
      const title = `Aaj ki ${publishedCount} nayi shayari ❤️`;
      const body = "EhsaasVerse open karein aur nayi shayari parhein";
      
      const message = {
        notification: {
          title: title,
          body: body
        },
        topic: topic
      };

      try {
        const response = await messaging.send(message);
        console.log('Successfully sent push notification:', response);
        
        // Log notification to history collection
        await db.collection('notifications').add({
          title: title,
          body: body,
          type: 'all',
          sentAt: FieldValue.serverTimestamp()
        });
      } catch (err) {
        console.error('Error sending push notification:', err);
      }
    }

    console.log("Job completed successfully.");
    process.exit(0);
  } catch (error) {
    console.error("Error running auto publish script:", error);
    process.exit(1);
  }
}

run();
