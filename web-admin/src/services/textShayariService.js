import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, serverTimestamp, query, orderBy, where, writeBatch } from 'firebase/firestore';
import { db } from '../firebase/config';

const COLLECTION_NAME = 'text_shayari';
const shayariCol = collection(db, COLLECTION_NAME);

export const getTextShayaris = async (categoryId = null) => {
  let q = query(shayariCol, orderBy('createdAt', 'desc'));
  if (categoryId) {
    q = query(shayariCol, where('categoryId', '==', categoryId), orderBy('createdAt', 'desc'));
  }
  const snapshot = await getDocs(q);
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
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
