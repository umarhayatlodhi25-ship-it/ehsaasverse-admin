import { initializeApp, cert } from 'firebase-admin/app';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';
import { GoogleGenerativeAI } from '@google/generative-ai';
import * as fs from 'fs';
import * as path from 'path';

// For GitHub Actions, we read from env variables.
// If running locally, we try to read from firebase_deploy_key.json
let serviceAccount;
try {
  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
  } else {
    serviceAccount = JSON.parse(fs.readFileSync(path.join(process.cwd(), '..', 'firebase_deploy_key.json'), 'utf8'));
  }
} catch (e) {
  console.error("Error loading service account. Make sure FIREBASE_SERVICE_ACCOUNT secret is set.");
  process.exit(1);
}

const app = initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore(app);

const apiKey = process.env.GEMINI_API_KEY;
if (!apiKey) {
  console.error("Error: GEMINI_API_KEY is not set.");
  process.exit(1);
}
const genAI = new GoogleGenerativeAI(apiKey);
const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

async function runDailyAutomation() {
  console.log("Starting Daily Auto Generation...");
  try {
    const catsSnapshot = await db.collection("categories").where("type", "!=", "photo").get();
    const categories = [];
    catsSnapshot.forEach(doc => {
      categories.push({ id: doc.id, name: doc.data().name });
    });

    console.log(`Found ${categories.length} text categories.`);

    for (const cat of categories) {
      console.log(`Generating 10 shayaris for category: ${cat.name}...`);
      
      const prompt = `Write 10 unique, deep, poetic, and high-quality 2-line Urdu shayaris (in Urdu script) for the category: "${cat.name}".
      Return ONLY the plain Urdu text. Separate each shayari with EXACTLY two blank lines (\\n\\n\\n). Do NOT number them. No english translation. No quotes.`;
      
      try {
        const result = await model.generateContent(prompt);
        const text = result.response.text().trim().replace(/['"]/g, '');
        const shayaris = text.split(/\n{2,}/).map(s => s.trim().replace(/\n/g, '  ')).filter(s => s.length > 5);
        
        console.log(`Generated ${shayaris.length} valid shayaris for ${cat.name}. Adding to database...`);
        
        const batch = db.batch();
        for (const shayariText of shayaris) {
          const newDocRef = db.collection("text_shayari").doc();
          batch.set(newDocRef, {
            content: shayariText,
            categoryId: cat.id,
            categoryName: cat.name,
            mehfilCategoryId: null,
            subCategoryId: null,
            status: 'published',
            createdAt: Timestamp.now(),
            updatedAt: Timestamp.now()
          });
        }
        await batch.commit();
        console.log(`✅ Successfully added ${shayaris.length} shayaris for ${cat.name}.`);
      } catch (err) {
        console.error(`❌ Failed to generate for ${cat.name}:`, err.message);
      }

      // Wait 3 seconds between categories to respect rate limits
      await new Promise(r => setTimeout(r, 3000));
    }
    
    console.log("Daily Auto Generation Complete!");
    process.exit(0);
  } catch (error) {
    console.error("Fatal Error:", error);
    process.exit(1);
  }
}

runDailyAutomation();
