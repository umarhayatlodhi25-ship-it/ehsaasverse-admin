const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('../firebase_deploy_key.json');

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

async function migrate() {
    console.log("Migrating verse_official_v1 to text_shayari...");
    const oldSnapshot = await db.collection('verse_official_v1').get();
    console.log(`Found ${oldSnapshot.size} records.`);
    
    let count = 0;
    let batch = db.batch();
    
    for (const doc of oldSnapshot.docs) {
        const data = doc.data();
        
        const categoryName = data.category || 'General';
        const categoryId = categoryName.toLowerCase().replace(/[^a-z0-9]/g, '-');
        
        const newRef = db.collection('text_shayari').doc();
        batch.set(newRef, {
            content: data.urdu || '',
            categoryId: categoryId,
            categoryName: categoryName,
            status: 'published',
            createdAt: new Date(data.timestamp || Date.now()),
            updatedAt: new Date()
        });
        
        count++;
        if (count % 400 === 0) {
            await batch.commit();
            console.log(`Committed ${count} records...`);
            batch = db.batch(); // Create new batch
        }
    }
    
    if (count % 400 !== 0) {
        await batch.commit();
    }
    
    console.log(`Successfully migrated ${count} text shayaris!`);
}

migrate().then(() => process.exit(0)).catch(console.error);
