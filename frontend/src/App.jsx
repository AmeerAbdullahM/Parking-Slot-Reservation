import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import CustomerDashboard from './pages/CustomerDashboard';
import ManagerDashboard from './pages/ManagerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import { Car } from 'lucide-react';

function MainApp() {
  const { user, role, loading } = useAuth();
  const [activeTab, setActiveTab] = useState('default');

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        minHeight: '100vh',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
        gap: '1rem',
        color: '#38bdf8'
      }}>
        <Car size={40} className="animate-pulse" />
        <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Loading ParkPulse Engine...</div>
      </div>
    );
  }

  if (!user) {
    return <LoginPage />;
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <main style={{ flex: 1 }}>
        {role === 'ROLE_ADMIN' && <AdminDashboard activeTab={activeTab} />}
        {role === 'ROLE_MANAGER' && <ManagerDashboard activeTab={activeTab} />}
        {role === 'ROLE_CUSTOMER' && <CustomerDashboard activeTab={activeTab} setActiveTab={setActiveTab} />}
      </main>
      <footer style={{
        textAlign: 'center',
        padding: '1.5rem',
        color: 'var(--text-dim)',
        fontSize: '0.8rem',
        borderTop: '1px solid var(--border-subtle)'
      }}>
        ParkPulse Smart Parking Slot Reservation System • Multi-Role Enterprise Platform
      </footer>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
}
