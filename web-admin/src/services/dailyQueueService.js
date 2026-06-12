import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, serverTimestamp, query, orderBy, where, writeBatch, limit } from 'firebase/firestore';
import { db } from '../firebase/config';
import { addTextShayari } from './textShayariService';
import { addPhotoShayari } from './photoShayariService';

const COLLECTION_NAME = 'daily_queue';
const queueCol = collection(db, COLLECTION_NAME);

export const getQueueItems = async () => {
  const q = query(queueCol, orderBy('createdAt', 'asc'));
  const snapshot = await getDocs(q);
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
};

export const addToQueue = async (data) => {
  return await addDoc(queueCol, {
    ...data,
    status: 'pending',
    createdAt: serverTimestamp()
  });
};

export const deleteFromQueue = async (id) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await deleteDoc(docRef);
};

export const publishDailyShayaris = async (count = 10) => {
  const q = query(queueCol, where('status', '==', 'pending'), orderBy('createdAt', 'asc'), limit(count));
  const snapshot = await getDocs(q);
  
  const items = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  
  for (const item of items) {
    // Add to actual collection
    if (item.type === 'photo') {
      await addPhotoShayari({
        imageUrl: item.imageUrl,
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        status: 'published',
        isDaily: true,
        publishDate: new Date().toISOString()
      });
    } else {
      await addTextShayari({
        content: item.content,
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        status: 'published',
        isDaily: true,
        publishDate: new Date().toISOString()
      });
    }
    
    // Mark as published in queue
    const docRef = doc(db, COLLECTION_NAME, item.id);
    await updateDoc(docRef, { status: 'published', publishedAt: serverTimestamp() });
  }
  
  return items.length;
};
