import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';
import { getMessaging } from 'firebase-admin/messaging';
import { createClient } from '@supabase/supabase-js';
import { GoogleGenerativeAI } from '@google/generative-ai';

const supabase = createClient(
  "https://navwtxcpfllvsthidwew.supabase.co",
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hdnd0eGNwZmxsdnN0aGlkd2V3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODEyODI2NTYsImV4cCI6MjA5Njg1ODY1Nn0.PQoeivttm3vJyhNmD0oi1wriBVTC1He1jLAlrcI6H-g"
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
const QUEUE_MIN_SIZE = 5; // Minimum pending items to keep in queue (auto-fill if below)
const FALLBACK_CATEGORIES = ['Motivation', 'Intezar', 'Love', 'Khwab', 'Dua', 'Aansu', 'Bewafa', 'Friendship', 'Ishq', 'Barish', 'Mashhoor', 'Tanhai', 'Judai', 'Sad', 'Zindagi', 'Yaad', 'Dard'];

async function generateAIShayari() {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error("GEMINI_API_KEY missing. Cannot use AI fallback.");
  }
  const genAI = new GoogleGenerativeAI(apiKey);
  // Using gemini-2.5-flash as it's fast and reliable for simple generation
  const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });
  
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

    // 3. AI Fallback for missing published items
    const missingCount = PUBLISH_COUNT - publishedCount;
    if (missingCount > 0) {
      console.log(`Queue is short. Generating ${missingCount} shayaris using AI and publishing directly...`);
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

    // 4. Auto-fill Daily Queue with AI if queue is running low
    const pendingSnapshot = await db.collection('daily_queue')
      .where('status', '==', 'pending')
      .limit(QUEUE_MIN_SIZE + 1)
      .get();
    const pendingCount = pendingSnapshot.size;
    console.log(`Current pending items in Daily Queue: ${pendingCount}`);

    if (pendingCount < QUEUE_MIN_SIZE) {
      const toAdd = QUEUE_MIN_SIZE - pendingCount;
      console.log(`Queue is low! Auto-filling ${toAdd} AI shayaris into Daily Queue...`);
      for (let i = 0; i < toAdd; i++) {
        try {
          const aiData = await generateAIShayari();
          await db.collection('daily_queue').add({
            type: 'text',
            content: aiData.content,
            categoryId: aiData.categoryName.toLowerCase(),
            categoryName: aiData.categoryName,
            status: 'pending',
            isAiGenerated: true,
            createdAt: FieldValue.serverTimestamp()
          });
          console.log(`Added AI shayari to Daily Queue: ${aiData.categoryName}`);
        } catch (qErr) {
          console.error("Failed to add AI shayari to queue:", qErr.message);
        }
      }
    } else {
      console.log('Daily Queue has enough pending items. No AI fill needed.');
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
