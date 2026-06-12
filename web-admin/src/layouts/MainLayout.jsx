import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  FileText,
  Image as ImageIcon,
  Layers,
  MessageSquare,
  Settings,
  LogOut,
  Menu,
  X
} from 'lucide-react';
import { useState } from 'react';
import { signOut } from 'firebase/auth';
import { auth } from '../firebase/config';

const MainLayout = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async () => {
    await signOut(auth);
    navigate('/login');
  };

  const navItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/text-shayari', icon: FileText, label: 'Text Shayari' },
    { to: '/photo-shayari', icon: ImageIcon, label: 'Photo Shayari' },
    { to: '/categories', icon: Layers, label: 'Categories' },
    { to: '/daily-queue', icon: MessageSquare, label: 'Daily Queue' },
    { to: '/notifications', icon: Settings, label: 'Notifications' },
  ];

  return (
    <div className="flex h-screen bg-transparent overflow-hidden">
      {/* Sidebar Desktop */}
      <aside className="hidden md:flex flex-col w-64 bg-surface text-white shadow-2xl border-r border-primary-light/10">
        <div className="p-6">
          <h1 className="text-2xl font-bold tracking-wider text-gold-light drop-shadow-md">EhsaasVerse</h1>
          <p className="text-xs text-gold/50 mt-1 uppercase">Admin Control Panel</p>
        </div>

        <nav className="flex-1 px-4 py-4 space-y-2 overflow-y-auto">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `
                flex items-center px-4 py-3 rounded-xl transition-all duration-200
                ${isActive
                  ? 'bg-gradient-to-r from-gold to-gold-dark text-background font-semibold shadow-[0_0_15px_rgba(229,198,138,0.3)] scale-105'
                  : 'hover:bg-primary-dark/50 text-white/70 hover:text-white'}
              `}
            >
              <item.icon className="w-5 h-5 mr-3" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-primary-light/10">
          <button
            onClick={handleLogout}
            className="flex items-center w-full px-4 py-3 text-red-400 hover:bg-red-500/10 rounded-xl transition-colors"
          >
            <LogOut className="w-5 h-5 mr-3" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden relative">
        {/* Header */}
        <header className="bg-surface/40 backdrop-blur-md border-b border-primary-light/20 px-4 md:px-8 py-4 flex items-center justify-between shadow-sm z-10">
          <button
            className="md:hidden p-2 text-gold hover:bg-primary/20 rounded-lg"
            onClick={() => setIsMobileMenuOpen(true)}
          >
            <Menu className="w-6 h-6" />
          </button>

          <div className="flex items-center space-x-4 ml-auto">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-semibold text-white">Admin Panel</p>
              <p className="text-xs text-gold/70">Master Control</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-gold to-gold-dark flex items-center justify-center text-background font-bold shadow-lg">
              A
            </div>
          </div>
        </header>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-4 md:p-8">
          <Outlet />
        </div>
      </main>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <div className="fixed inset-0 z-50 md:hidden bg-black/50 backdrop-blur-sm" onClick={() => setIsMobileMenuOpen(false)}>
          <aside
            className="w-64 h-full bg-primary-dark text-white flex flex-col p-6 animate-slide-right"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-8">
              <h1 className="text-xl font-bold text-gold">EhsaasVerse</h1>
              <button onClick={() => setIsMobileMenuOpen(false)}><X className="w-6 h-6" /></button>
            </div>
            <nav className="flex-1 space-y-4">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setIsMobileMenuOpen(false)}
                  className={({ isActive }) => `flex items-center p-3 rounded-lg ${isActive ? 'bg-gold text-primary-dark' : ''}`}
                >
                  <item.icon className="w-5 h-5 mr-3" />
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </aside>
        </div>
      )}
    </div>
  );
};

export default MainLayout;
