import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Car, Shield, UserCog, User, LogOut, Layers, Ticket, Grid } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab }) {
  const { user, role, logout } = useAuth();

  return (
    <header className="navbar">
      <div className="brand-logo" onClick={() => setActiveTab('default')}>
        <div className="brand-icon">
          <Car size={22} />
        </div>
        <span>Park<span style={{ color: '#3b82f6' }}>Pulse</span></span>
      </div>

      {user && (
        <nav style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          {role === 'ROLE_CUSTOMER' && (
            <>
              <button
                className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('browse')}
              >
                <Grid size={16} /> Browse Facilities
              </button>
              <button
                className={`btn ${activeTab === 'passes' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('passes')}
              >
                <Ticket size={16} /> My Passes
              </button>
              <button
                className={`btn ${activeTab === 'vehicles' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('vehicles')}
              >
                <Car size={16} /> Vehicles
              </button>
            </>
          )}

          {role === 'ROLE_MANAGER' && (
            <>
              <button
                className={`btn ${activeTab === 'manager-map' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('manager-map')}
              >
                <Layers size={16} /> 2D Bay Builder
              </button>
              <button
                className={`btn ${activeTab === 'manager-bookings' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('manager-bookings')}
              >
                <Ticket size={16} /> Arrivals & Check-In
              </button>
            </>
          )}

          {role === 'ROLE_ADMIN' && (
            <>
              <button
                className={`btn ${activeTab === 'admin-stats' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('admin-stats')}
              >
                <Shield size={16} /> System Metrics
              </button>
              <button
                className={`btn ${activeTab === 'admin-managers' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('admin-managers')}
              >
                <UserCog size={16} /> Provision Managers
              </button>
              <button
                className={`btn ${activeTab === 'admin-facilities' ? 'btn-primary' : 'btn-ghost'}`}
                onClick={() => setActiveTab('admin-facilities')}
              >
                <Layers size={16} /> Facilities
              </button>
            </>
          )}
        </nav>
      )}

      {user && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.88rem', fontWeight: 700 }}>{user.fullName}</div>
            <span className={`badge ${
              role === 'ROLE_ADMIN' ? 'badge-admin' : role === 'ROLE_MANAGER' ? 'badge-manager' : 'badge-customer'
            }`}>
              {role === 'ROLE_ADMIN' ? 'Admin' : role === 'ROLE_MANAGER' ? 'Mall Manager' : 'Customer'}
            </span>
          </div>

          <button className="btn btn-ghost" onClick={logout} title="Sign Out">
            <LogOut size={18} />
          </button>
        </div>
      )}
    </header>
  );
}
