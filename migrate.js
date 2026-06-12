const fs = require('fs');
const { initializeApp } = require('firebase/app');
const { getFirestore, collection, addDoc, getDocs, query, where } = require('firebase/firestore');
const { createClient } = require('@supabase/supabase-js');

// Config from project details
const firebaseConfig = {
  apiKey: "AIzaSyBKowDs1MHYae5V0ZdvGhR2WnWvamMwFEY",
  authDomain: "ehsaasverse-14d5d.firebaseapp.com",
  projectId: "ehsaasverse-14d5d",
  storageBucket: "ehsaasverse-14d5d.firebasestorage.app",
  messagingSenderId: "825232716466",
  appId: "1:825232716466:web:f2c1f6574d3eb063680fab"
};

const supabaseUrl = "https://jgdrdtirmuvkoznfuuog.supabase.co";
const supabaseKey = "sb_publishable_23VXzWVp2ofyGLegPbOMBw_rElW679g";

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const supabase = createClient(supabaseUrl, supabaseKey);

async function migrateTextShayari() {
    console.log("Migrating Text Shayari...");
    try {
        const data = JSON.parse(fs.readFileSync('./app/src/main/assets/shayari.json', 'utf8'));
        console.log(`Found ${data.length} items to migrate.`);

        for (const item of data) {
            // Check for duplicates
            const q = query(collection(db, "text_shayari"), where("content", "==", item.urdu));
            const snap = await getDocs(q);

            if (snap.empty) {
                await addDoc(collection(db, "text_shayari"), {
                    title: item.roman ? item.roman.substring(0, 50) : "Untitled",
                    content: item.urdu,
                    category: item.category,
                    author: item.poet || "Unknown",
                    createdAt: item.timestamp || Date.now(),
                    updatedAt: Date.now(),
                    status: "published"
                });
                console.log(`✅ Migrated: ${item.urdu.substring(0, 30)}...`);
            } else {
                console.log(`⚠️ Skipped duplicate: ${item.urdu.substring(0, 30)}...`);
            }
        }
    } catch (err) {
        console.error("Text Migration Error:", err.message);
    }
}

async function migratePhotoShayari() {
    console.log("\nMigrating Photo Shayari...");
    try {
        const data = JSON.parse(fs.readFileSync('./app/src/main/assets/photo_shayari.json', 'utf8'));
        console.log(`Found ${data.length} items to migrate.`);

        for (const item of data) {
            // Duplicate check
            const { data: existing, error } = await supabase
                .from('image_shayari')
                .select('id')
                .eq('image_url', item.imageUrl);

            if (!error && existing.length === 0) {
                const { error: insErr } = await supabase
                    .from('image_shayari')
                    .insert([{
                        title: item.roman ? item.roman.substring(0, 50) : "Untitled",
                        category: item.category,
                        image_url: item.imageUrl,
                        status: "published"
                    }]);

                if (insErr) console.error(`❌ Error inserting ${item.imageUrl}:`, insErr.message);
                else console.log(`✅ Migrated Photo: ${item.imageUrl}`);
            } else {
                console.log(`⚠️ Skipped duplicate Photo: ${item.imageUrl}`);
            }
        }
    } catch (err) {
        console.error("Photo Migration Error:", err.message);
    }
}

async function start() {
    console.log("Starting Migration Process for EhsaasVerse...\n");
    await migrateTextShayari();
    await migratePhotoShayari();
    console.log("\nMigration Process Finished!");
    process.exit(0);
}

start();
