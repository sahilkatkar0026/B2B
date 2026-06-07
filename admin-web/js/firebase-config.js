/**
 * Firebase configuration and initialization
 * Medical B2B Admin Panel - Firebase Realtime Database + Auth
 * SDK: Firebase Modular v10+
 */
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-app.js';
import { getAuth } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-auth.js';
import { getDatabase } from 'https://www.gstatic.com/firebasejs/10.7.0/firebase-database.js';

const firebaseConfig = {
  apiKey: 'AIzaSyBf7PLvj0tyQo5QAqK_BEq15WQQXueYkqM',
  authDomain: 'medicalshopb2b.firebaseapp.com',
  databaseURL: 'https://medicalshopb2b-default-rtdb.firebaseio.com',
  projectId: 'medicalshopb2b',
  storageBucket: 'medicalshopb2b.firebasestorage.app',
  messagingSenderId: '227221936412',
  appId: '1:227221936412:web:867083e8b83503d39dc2d9',
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getDatabase(app);

export { app, auth, db };
