import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, serverTimestamp, query, orderBy } from 'firebase/firestore';
import { db } from '../firebase/config';

const COLLECTION_NAME = 'categories';
const categoriesCol = collection(db, COLLECTION_NAME);

export const getCategories = async () => {
  const q = query(categoriesCol, orderBy('createdAt', 'asc'));
  const snapshot = await getDocs(q);
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
};

export const addCategory = async (categoryData) => {
  return await addDoc(categoriesCol, {
    ...categoryData,
    createdAt: serverTimestamp()
  });
};

export const updateCategory = async (id, categoryData) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await updateDoc(docRef, categoryData);
};

export const deleteCategory = async (id) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await deleteDoc(docRef);
};
