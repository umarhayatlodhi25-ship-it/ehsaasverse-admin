import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc, serverTimestamp, query, orderBy, where, writeBatch } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from '../firebase/config';
import { v4 as uuidv4 } from 'uuid';

const COLLECTION_NAME = 'photo_shayari';
const shayariCol = collection(db, COLLECTION_NAME);

export const getPhotoShayaris = async (categoryId = null) => {
  let q = query(shayariCol, orderBy('createdAt', 'desc'));
  if (categoryId) {
    q = query(shayariCol, where('categoryId', '==', categoryId), orderBy('createdAt', 'desc'));
  }
  const snapshot = await getDocs(q);
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
};

export const uploadImage = async (file) => {
  const fileExt = file.name.split('.').pop();
  const fileName = `${uuidv4()}.${fileExt}`;
  const storageRef = ref(storage, `photo_shayari/${fileName}`);
  await uploadBytes(storageRef, file);
  return await getDownloadURL(storageRef);
};

export const addPhotoShayari = async (data) => {
  return await addDoc(shayariCol, {
    ...data,
    status: data.status || 'published',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const updatePhotoShayari = async (id, data) => {
  const docRef = doc(db, COLLECTION_NAME, id);
  return await updateDoc(docRef, { ...data, updatedAt: serverTimestamp() });
};

export const deletePhotoShayari = async (id, imageUrl) => {
  if (imageUrl) {
    try {
      const storageRef = ref(storage, imageUrl);
      await deleteObject(storageRef);
    } catch (e) {
      console.error("Error deleting image from storage", e);
    }
  }
  const docRef = doc(db, COLLECTION_NAME, id);
  return await deleteDoc(docRef);
};
