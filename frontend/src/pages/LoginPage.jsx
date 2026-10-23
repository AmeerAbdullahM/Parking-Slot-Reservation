import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Car, Lock, Mail, User, Phone, ArrowRight, ShieldCheck, Zap, AlertCircle } from 'lucide-react';

export default function LoginPage() {
  const { login, register } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Login state
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Register state
  const [fullName, setFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [phone, setPhone] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
    } catch (err) {
      setError(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(fullName, regEmail, regPassword, phone);
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fillQuickCredentials = (e, p) => {
    setEmail(e);
    setPassword(p);
    setIsRegister(false);
  };

  return (
    <div style={{
      display: 'flex',
      minHeight: 'calc(100vh - 75px)',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem 1rem'
    }}>
      <div className="glass-panel" style={{
        maxWidth: '480px',
        width: '100%',
        padding: '2.5rem',
        boxShadow: '0 20px 40px rgba(0,0,0,0.5)'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            display: 'inline-flex',
            padding: '1rem',
            background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(6, 182, 212, 0.2))',
            borderRadius: '16px',
            color: '#38bdf8',
            marginBottom: '1rem'
          }}>
            <Car size={36} />
          </div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, letterSpacing: '-0.02em' }}>
            {isRegister ? 'Create Customer Account' : 'Unified Portal Login'}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.4rem' }}>
            {isRegister
              ? 'Reserve parking slots in malls & campuses in seconds'
              : 'Admin, Mall Manager, & Customer — access your respective dashboard'}
          </p>
        </div>

        {error && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.65rem',
            padding: '0.85rem 1rem',
            background: 'rgba(244, 63, 94, 0.15)',
            border: '1px solid rgba(244, 63, 94, 0.3)',
            borderRadius: '10px',
            color: '#fb7185',
            fontSize: '0.85rem',
            marginBottom: '1.5rem'
          }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {!isRegister ? (
          <form onSubmit={handleLogin}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="email"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="name@parkpulse.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <Lock size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="password"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', padding: '0.85rem', marginTop: '0.5rem' }}
              disabled={loading}
            >
              {loading ? 'Authenticating...' : <><span>Sign In to Dashboard</span> <ArrowRight size={16} /></>}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <div style={{ position: 'relative' }}>
                <User size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="text"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="Alex Mercer"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="email"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="alex@example.com"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <div style={{ position: 'relative' }}>
                <Phone size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="tel"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="+1-555-0199"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Create Password</label>
              <div style={{ position: 'relative' }}>
                <Lock size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: 'var(--text-dim)' }} />
                <input
                  type="password"
                  className="form-input"
                  style={{ paddingLeft: '2.5rem' }}
                  placeholder="Minimum 6 characters"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  required
                  minLength={6}
                />
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-emerald"
              style={{ width: '100%', padding: '0.85rem', marginTop: '0.5rem' }}
              disabled={loading}
            >
              {loading ? 'Creating Account...' : <><span>Complete Registration</span> <ArrowRight size={16} /></>}
            </button>
          </form>
        )}

        <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
          {!isRegister ? (
            <span>
              New driver?{' '}
              <button
                type="button"
                style={{ background: 'none', border: 'none', color: '#38bdf8', fontWeight: 600, cursor: 'pointer' }}
                onClick={() => setIsRegister(true)}
              >
                Register as Customer
              </button>
            </span>
          ) : (
            <span>
              Already registered?{' '}
              <button
                type="button"
                style={{ background: 'none', border: 'none', color: '#38bdf8', fontWeight: 600, cursor: 'pointer' }}
                onClick={() => setIsRegister(false)}
              >
                Back to Sign In
              </button>
            </span>
          )}
        </div>

        {/* Quick-Fill Chips for Viva Evaluators */}
        <div style={{
          marginTop: '2rem',
          paddingTop: '1.5rem',
          borderTop: '1px dashed var(--border-subtle)',
          fontSize: '0.8rem'
        }}>
          <div style={{ color: 'var(--text-dim)', marginBottom: '0.65rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Zap size={14} color="#f59e0b" />
            <span>Quick Login for Testing & Viva Defense:</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            <button
              type="button"
              className="btn btn-ghost"
              style={{ fontSize: '0.75rem', padding: '0.35rem 0.65rem' }}
              onClick={() => fillQuickCredentials('admin@parkpulse.com', 'password123')}
            >
              🛡️ Admin
            </button>
            <button
              type="button"
              className="btn btn-ghost"
              style={{ fontSize: '0.75rem', padding: '0.35rem 0.65rem' }}
              onClick={() => fillQuickCredentials('manager.mall@parkpulse.com', 'password123')}
            >
              🏬 Mall Manager
            </button>
            <button
              type="button"
              className="btn btn-ghost"
              style={{ fontSize: '0.75rem', padding: '0.35rem 0.65rem' }}
              onClick={() => fillQuickCredentials('customer@parkpulse.com', 'password123')}
            >
              🚗 Customer
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
