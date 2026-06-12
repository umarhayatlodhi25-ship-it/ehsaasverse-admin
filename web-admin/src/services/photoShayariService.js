import { collection, getDocs, addDoc, deleteDoc, doc, serverTimestamp, query, orderBy, where, limit } from 'firebase/firestore';
import { ref, uploadBytesResumable, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from '../firebase/config';
import { v4 as uuidv4 } from 'uuid';

const COLLECTION_NAME = 'image_shayari';
const photoCol = collection(db, COLLECTION_NAME);

export const getPhotoShayaris = async (categoryName = null) => {
  let q;
  if (categoryName) {
    q = query(photoCol, where('categoryName', '==', categoryName), limit(200));
  } else {
    q = query(photoCol, orderBy('createdAt', 'desc'), limit(200));
  }
  const snapshot = await getDocs(q);
  let results = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));

  if (categoryName) {
    results.sort((a, b) => {
      const timeA = a.createdAt?.toMillis ? a.createdAt.toMillis() : 0;
      const timeB = b.createdAt?.toMillis ? b.createdAt.toMillis() : 0;
      return timeB - timeA;
    });
  }
  return results;
};

export const uploadImage = async (file) => {
  const fileExt = file.name.split('.').pop();
  const fileName = `ehsaasverse-images/${uuidv4()}.${fileExt}`;
  const storageRef = ref(storage, fileName);
  
  const snapshot = await uploadBytesResumable(storageRef, file);
  const downloadURL = await getDownloadURL(snapshot.ref);
  return downloadURL;
};

export const addPhotoShayari = async (data) => {
  return await addDoc(photoCol, {
    imageUrl: data.imageUrl,
    caption: data.caption || '',
    categoryId: data.categoryId,
    categoryName: data.categoryName,
    status: data.status || 'published',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
};

export const deletePhotoShayari = async (id, imageUrl) => {
  // Delete from Firebase Storage
  if (imageUrl) {
    try {
      const storageRef = ref(storage, imageUrl);
      await deleteObject(storageRef);
    } catch (err) {
      console.warn('Could not delete from storage (may already be gone):', err.message);
    }
  }
  // Delete from Firestore
  const docRef = doc(db, COLLECTION_NAME, id);
  return await deleteDoc(docRef);
};
