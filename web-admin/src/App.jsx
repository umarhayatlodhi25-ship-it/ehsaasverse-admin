import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './firebase/config';
import { Toaster } from 'react-hot-toast';

// Layouts & Pages
import MainLayout from './layouts/MainLayout';
import Dashboard from './pages/Dashboard';
import TextShayari from './pages/TextShayari';
import PhotoShayari from './pages/PhotoShayari';
import Categories from './pages/Categories';
import DailyQueue from './pages/DailyQueue';
import Notifications from './pages/Notifications';
import Settings from './pages/Settings';
import Login from './pages/Login';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      if (currentUser) {
        setUser(currentUser);
      } else {
        setUser(null);
      }
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  if (loading) return (
    <div className="h-screen w-screen flex items-center justify-center bg-primary">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
    </div>
  );

  return (
    <>
      <Toaster position="top-right" />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={!user ? <Login /> : <Navigate to="/" />} />

          <Route element={user ? <MainLayout /> : <Navigate to="/login" />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/text-shayari" element={<TextShayari />} />
            <Route path="/photo-shayari" element={<PhotoShayari />} />
            <Route path="/categories" element={<Categories />} />
            <Route path="/daily-queue" element={<DailyQueue />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/settings" element={<Settings />} />
          </Route>

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;

