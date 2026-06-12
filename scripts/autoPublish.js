import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';
import { getMessaging } from 'firebase-admin/messaging';
import { createClient } from '@supabase/supabase-js';
import { GoogleGenerativeAI } from '@google/generative-ai';

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
const FALLBACK_CATEGORIES = ['Motivation', 'Intezar', 'Love', 'Khwab', 'Dua', 'Aansu', 'Bewafa', 'Friendship', 'Ishq', 'Barish', 'Mashhoor', 'Tanhai', 'Judai', 'Sad', 'Zindagi', 'Yaad', 'Dard'];

async function generateAIShayari() {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error("GEMINI_API_KEY missing. Cannot use AI fallback.");
  }
  const genAI = new GoogleGenerativeAI(apiKey);
  // Using gemini-1.5-flash as it's fast and reliable for simple generation
  const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });
  
  const randomCategory = FALLBACK_CATEGORIES[Math.floor(Math.random() * FALLBACK_CATEGORIES.length)];
  const prompt = `Write a deep, poetic, and high-quality 2-line Urdu shayari (in Urdu script) for the category: "${randomCategory}".
  Return ONLY a raw JSON object with this exact structure: {"content": "urdu text here", "categoryName": "${randomCategory}"}. No markdown blocks.`;

  const result = await model.generateContent(prompt);
  let responseText = result.response.text();
  
  try {
    // Clean potential markdown from output
    responseText = responseText.replace(/```json/gi, '').replace(/```/g, '').trim();
    const data = JSON.parse(responseText);
    if (!data.content || !data.categoryName) throw new Error("Invalid schema returned by AI");
    return data;
  } catch (err) {
    console.error("Failed to parse AI output:", responseText);
    throw err;
  }
}

async function run() {
  console.log(`Starting Auto Publish Job. Target: ${PUBLISH_COUNT} shayaris...`);
  
  try {
    // 1. Fetch pending items from daily_queue
    const queueRef = db.collection('daily_queue');
    const snapshot = await queueRef
      .where('status', '==', 'pending')
      .limit(PUBLISH_COUNT)
      .get();

    console.log(`Found ${snapshot.size} pending items in manual queue.`);
    
    let publishedCount = 0;

    // 2. Process manual items
    if (!snapshot.empty) {
      for (const doc of snapshot.docs) {
        const item = doc.data();
        const payload = {
          categoryId: item.categoryId || (item.categoryName || 'Unknown').toLowerCase(),
          categoryName: item.categoryName || 'Unknown',
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
              continue;
          }
        } else {
          payload.content = item.content;
          await db.collection('text_shayari').add(payload);
        }

        // Mark as published
        await doc.ref.update({
          status: 'published',
          publishedAt: FieldValue.serverTimestamp()
        });
        
        publishedCount++;
      }
    }

    // 3. AI Fallback for missing items
    const missingCount = PUBLISH_COUNT - publishedCount;
    if (missingCount > 0) {
      console.log(`Queue is short. Generating ${missingCount} shayaris using AI...`);
      for (let i = 0; i < missingCount; i++) {
        try {
          const aiData = await generateAIShayari();
          const payload = {
            categoryId: aiData.categoryName.toLowerCase(),
            categoryName: aiData.categoryName,
            content: aiData.content,
            status: 'published',
            isDaily: true,
            isAiGenerated: true,
            publishDate: new Date().toISOString(),
            createdAt: FieldValue.serverTimestamp()
          };
          
          await db.collection('text_shayari').add(payload);
          console.log(`Successfully generated and published AI shayari for category: ${aiData.categoryName}`);
          publishedCount++;
        } catch (aiErr) {
          console.error("AI Generation failed for this iteration:", aiErr.message);
        }
      }
    }

    console.log(`Successfully published ${publishedCount} shayaris in total.`);

    // 4. Send Push Notification
    if (publishedCount > 0) {
      const topic = 'all';
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
