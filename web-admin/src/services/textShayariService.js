import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, serverTimestamp, query, orderBy, where, writeBatch, limit } from 'firebase/firestore';
import { db } from '../firebase/config';

const COLLECTION_NAME = 'text_shayari';
const shayariCol = collection(db, COLLECTION_NAME);

export const getTextShayaris = async (categoryName = null) => {
  let q;
  if (categoryName) {
    q = query(shayariCol, where('categoryName', '==', categoryName), limit(300));
  } else {
    q = query(shayariCol, orderBy('createdAt', 'desc'), limit(300));
  }
  const snapshot = await getDocs(q);
  let results = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  
  if (categoryName) {
    // Sort client-side to avoid needing a composite index in Firestore
    results.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : 0;
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : 0;
      return timeB - timeA;
    });
  }
  return results;
};

export const addTextShayari = async (data) => {
  return await addDoc(shayariCol, {
    ...data,
    status: data.status || 'published',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const updateTextShayari = async (id, data) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await updateDoc(docRef, { ...data, updatedAt: serverTimestamp() });
};

export const deleteTextShayari = async (id) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await deleteDoc(docRef);
};

export const bulkAddTextShayari = async (shayarisList) => {
  const batch = writeBatch(db);
  shayarisList.forEach((item) => {
    const docRef = doc(shayariCol);
    batch.set(docRef, {
      ...item,
      status: item.status || 'published',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
  });
  await batch.commit();
};
