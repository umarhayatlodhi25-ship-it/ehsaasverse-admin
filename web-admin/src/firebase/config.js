import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";
import { getStorage } from "firebase/storage";
import { getMessaging, isSupported } from "firebase/messaging";

const firebaseConfig = {
  apiKey: "AIzaSyBKowDs1MHYae5V0ZdvGhR2WnWvamMwFEY",
  authDomain: "ehsaasverse-14d5d.firebaseapp.com",
  projectId: "ehsaasverse-14d5d",
  storageBucket: "ehsaasverse-14d5d.firebasestorage.app",
  messagingSenderId: "825232716466",
  appId: "1:825232716466:web:f2c1f6574d3eb063680fab"
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
export const storage = getStorage(app);

// Initialize messaging conditionally to prevent errors if not supported
export let messaging = null;
isSupported().then((supported) => {
  if (supported) {
    messaging = getMessaging(app);
  }
});
