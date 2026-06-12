import { collection, getDocs, addDoc, serverTimestamp, query, orderBy } from 'firebase/firestore';
import { db } from '../firebase/config';

const COLLECTION_NAME = 'notifications';
const notifCol = collection(db, COLLECTION_NAME);

export const getNotifications = async () => {
  const q = query(notifCol, orderBy('sentAt', 'desc'));
  const snapshot = await getDocs(q);
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
};

export const sendNotification = async (title, body, categoryId = null) => {
  // Save notification to Firestore
  const docRef = await addDoc(notifCol, {
    title,
    body,
    categoryId,
    sentAt: serverTimestamp(),
    type: categoryId ? 'category_specific' : 'all'
  });

  // Note: Since FCM push notifications from a client web app directly to users requires Cloud Functions or a server with Admin SDK,
  // we are simulating the trigger here. In a real environment, you would call an HTTPS Callable function here.
  // Example: 
  // const sendPush = httpsCallable(functions, 'sendPushNotification');
  // await sendPush({ title, body, topic: categoryId || 'all' });
  
  return docRef.id;
};
