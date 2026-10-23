import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  Shield, UserCog, Building2, DollarSign, Layers, Plus,
  CheckCircle, AlertTriangle, Key, Copy, Check, Users, Search
} from 'lucide-react';

export default function AdminDashboard({ activeTab }) {
  const { user } = useAuth();

  // Metrics
  const [stats, setStats] = useState(null);
  const [managers, setManagers] = useState([]);
  const [lots, setLots] = useState([]);
  const [auditBookings, setAuditBookings] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);

  // Provision Manager Modal / Form
  const [isProvisionOpen, setIsProvisionOpen] = useState(false);
  const [mgrName, setMgrName] = useState('');
  const [mgrEmail, setMgrEmail] = useState('');
  const [mgrPassword, setMgrPassword] = useState('manager123');
  const [mgrPhone, setMgrPhone] = useState('+1-555-');
  const [mgrLotId, setMgrLotId] = useState('');
  const [provisionSuccess, setProvisionSuccess] = useState(null);
  const [provisionError, setProvisionError] = useState('');

  // Add Facility Modal
  const [isAddFacilityOpen, setIsAddFacilityOpen] = useState(false);
  const [facilityName, setFacilityName] = useState('');
  const [facilityType, setFacilityType] = useState('MALL');
  const [facilityAddress, setFacilityAddress] = useState('');
  const [facilityFloors, setFacilityFloors] = useState(3);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [statsData, managersData, lotsData, bookingsData] = await Promise.all([
        api.getAdminStats(),
        api.getAllManagers(),
        api.getLots(),
        api.searchReservations({ size: 50 })
      ]);

      setStats(statsData);
      setManagers(managersData || []);
      setLots(lotsData || []);
      setAuditBookings(bookingsData?.content || []);
      if (lotsData && lotsData.length > 0 && !mgrLotId) {
        setMgrLotId(lotsData[0].id);
      }
    } catch (err) {
      console.error('Failed to load admin metrics:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleProvisionManager = async (e) => {
    e.preventDefault();
    setProvisionError('');
    try {
      const response = await api.createManager({
        fullName: mgrName.trim(),
        email: mgrEmail.trim(),
        password: mgrPassword,
        phone: mgrPhone.trim(),
        assignedLotId: mgrLotId ? parseInt(mgrLotId) : null
      });

      setProvisionSuccess({
        fullName: response.fullName,
        email: response.email,
        password: mgrPassword,
        assignedLotName: lots.find((l) => l.id === parseInt(mgrLotId))?.name || 'General Assignment'
      });

      setIsProvisionOpen(false);
      setMgrName('');
      setMgrEmail('');
      loadDashboardData();
    } catch (err) {
      setProvisionError(err.message || 'Failed to provision manager account');
    }
  };

  const handleCreateFacility = async (e) => {
    e.preventDefault();
    try {
      await api.createLot({
        name: facilityName.trim(),
        locationType: facilityType,
        address: facilityAddress.trim(),
        totalFloors: parseInt(facilityFloors),
        totalCapacity: 0
      });
      setIsAddFacilityOpen(false);
      setFacilityName('');
      setFacilityAddress('');
      loadDashboardData();
    } catch (err) {
      alert(err.message || 'Failed to create facility');
    }
  };

  const copyCredentials = () => {
    if (!provisionSuccess) return;
    const text = `Mall Manager Access Credentials:\nPortal: ParkPulse\nEmail: ${provisionSuccess.email}\nPassword: ${provisionSuccess.password}\nAssigned Facility: ${provisionSuccess.assignedLotName}`;
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const filteredBookings = auditBookings.filter((b) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      b.reservationCode.toLowerCase().includes(q) ||
      b.userFullName.toLowerCase().includes(q) ||
      b.licensePlate.toLowerCase().includes(q) ||
      b.slotNumber.toLowerCase().includes(q)
    );
  });

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>

      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <span className="badge badge-admin">Master Administrator Portal</span>
            <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>• {user?.fullName}</span>
          </div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem' }}>
            System Infrastructure & Global Oversight
          </h1>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-ghost" onClick={() => setIsAddFacilityOpen(true)}>
            <Building2 size={16} /> New Facility
          </button>
          <button className="btn btn-primary" onClick={() => setIsProvisionOpen(true)}>
            <UserCog size={16} /> Provision Manager ID
          </button>
        </div>
      </div>

      {/* Provision Notification Card (when newly provisioned) */}
      {provisionSuccess && (
        <div className="glass-panel" style={{
          padding: '1.25rem',
          border: '1px solid rgba(59, 130, 246, 0.4)',
          background: 'rgba(30, 58, 138, 0.2)',
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#60a5fa', fontWeight: 700 }}>
              <CheckCircle size={18} />
              <span>Manager ID Successfully Provisioned for {provisionSuccess.assignedLotName}!</span>
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Email: <strong className="mono" style={{ color: 'var(--text-main)' }}>{provisionSuccess.email}</strong> | Password: <strong className="mono" style={{ color: 'var(--text-main)' }}>{provisionSuccess.password}</strong>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn btn-primary" style={{ padding: '0.45rem 0.85rem', fontSize: '0.8rem' }} onClick={copyCredentials}>
              {copied ? <Check size={14} /> : <Copy size={14} />} {copied ? 'Copied!' : 'Copy to Send Mall'}
            </button>
            <button className="btn btn-ghost" style={{ padding: '0.45rem' }} onClick={() => setProvisionSuccess(null)}>
              Dismiss
            </button>
          </div>
        </div>
      )}

      {/* Metric KPI Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1.25rem', marginBottom: '2.5rem' }}>
        <div className="glass-panel" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.8rem', fontWeight: 600 }}>FACILITIES</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem', color: '#38bdf8' }}>
            {stats?.totalLots || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Malls & Campuses Active</div>
        </div>

        <div className="glass-panel" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.8rem', fontWeight: 600 }}>TOTAL PARKING BAYS</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem', color: '#10b981' }}>
            {stats?.totalSlots || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Across all floors & decks</div>
        </div>

        <div className="glass-panel" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.8rem', fontWeight: 600 }}>TOTAL REVENUE</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem', color: '#34d399' }}>
            ${stats?.totalRevenue || '0.00'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Collected from bookings</div>
        </div>

        <div className="glass-panel" style={{ padding: '1.25rem' }}>
          <div style={{ color: 'var(--text-dim)', fontSize: '0.8rem', fontWeight: 600 }}>OVERSTAY PENALTIES</div>
          <div style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem', color: '#fb7185' }}>
            ${stats?.totalPenalties || '0.00'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Bonus engine collections</div>
        </div>
      </div>

      {/* SECTION: ASSIGNED MALL MANAGERS */}
      <div style={{ marginBottom: '3rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <UserCog size={20} color="#f59e0b" />
            <span>Mall & Facility Managers (Created by Admin)</span>
          </h2>
          <span style={{ fontSize: '0.82rem', color: 'var(--text-dim)' }}>
            Managers cannot self-register; credentials are issued here.
          </span>
        </div>

        <div className="glass-panel" style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-dim)' }}>
                <th style={{ padding: '1rem' }}>Manager Name</th>
                <th style={{ padding: '1rem' }}>Login Email</th>
                <th style={{ padding: '1rem' }}>Phone</th>
                <th style={{ padding: '1rem' }}>Assigned Facility</th>
                <th style={{ padding: '1rem' }}>Account Role</th>
              </tr>
            </thead>
            <tbody>
              {managers.map((m) => {
                const assignedLot = lots.find((l) => l.managerId === m.id);
                return (
                  <tr key={m.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '1rem', fontWeight: 700 }}>{m.fullName}</td>
                    <td style={{ padding: '1rem' }} className="mono">{m.email}</td>
                    <td style={{ padding: '1rem' }}>{m.phone || 'N/A'}</td>
                    <td style={{ padding: '1rem' }}>
                      <span style={{ color: '#38bdf8', fontWeight: 600 }}>
                        {assignedLot ? assignedLot.name : 'Unassigned Lot'}
                      </span>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span className="badge badge-manager">Manager</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* SECTION: GLOBAL RESERVATION AUDIT TABLE */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800 }}>Global Reservation & Overstay Audit Log</h2>
          <div style={{ position: 'relative', width: '280px' }}>
            <Search size={16} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
            <input
              type="text"
              className="form-input"
              style={{ paddingLeft: '2.3rem', fontSize: '0.85rem' }}
              placeholder="Search code, plate, or driver..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        <div className="glass-panel" style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-dim)' }}>
                <th style={{ padding: '1rem' }}>Code</th>
                <th style={{ padding: '1rem' }}>Facility & Bay</th>
                <th style={{ padding: '1rem' }}>Driver & Plate</th>
                <th style={{ padding: '1rem' }}>Status</th>
                <th style={{ padding: '1rem' }}>Overstay</th>
                <th style={{ padding: '1rem' }}>Base Fee</th>
                <th style={{ padding: '1rem' }}>Penalty</th>
                <th style={{ padding: '1rem' }}>Total Paid</th>
              </tr>
            </thead>
            <tbody>
              {filteredBookings.map((b) => (
                <tr key={b.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                  <td className="mono" style={{ padding: '1rem', fontWeight: 700, color: '#38bdf8' }}>
                    {b.reservationCode}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <div style={{ fontWeight: 600 }}>{b.lotName}</div>
                    <div className="mono" style={{ fontSize: '0.8rem', color: '#10b981' }}>Bay {b.slotNumber}</div>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <div>{b.userFullName}</div>
                    <div className="mono" style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>{b.licensePlate}</div>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <span className={`badge ${
                      b.status === 'CHECKED_IN' ? 'badge-admin' : b.status === 'CONFIRMED' ? 'badge-customer' : 'badge-manager'
                    }`}>
                      {b.status}
                    </span>
                  </td>
                  <td style={{ padding: '1rem' }}>
                    {b.overstayMinutes > 0 ? (
                      <span style={{ color: '#fb7185', fontWeight: 700 }}>+{b.overstayMinutes} min</span>
                    ) : (
                      <span style={{ color: 'var(--text-dim)' }}>On Time</span>
                    )}
                  </td>
                  <td style={{ padding: '1rem' }}>${b.baseFee}</td>
                  <td style={{ padding: '1rem', color: b.penaltyFee > 0 ? '#fb7185' : 'var(--text-dim)' }}>
                    ${b.penaltyFee}
                  </td>
                  <td style={{ padding: '1rem', fontWeight: 800, color: '#34d399' }}>
                    ${b.totalAmount}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* MODAL 1: PROVISION MANAGER ID */}
      {isProvisionOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Provision Mall Manager ID</h2>
              <button className="btn btn-ghost" style={{ padding: '0.35rem' }} onClick={() => setIsProvisionOpen(false)}>
                &times;
              </button>
            </div>

            {provisionError && (
              <div style={{ padding: '0.75rem', background: 'rgba(244,63,94,0.15)', color: '#fb7185', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem' }}>
                {provisionError}
              </div>
            )}

            <form onSubmit={handleProvisionManager}>
              <div className="form-group">
                <label className="form-label">Manager Full Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Marcus Vance"
                  value={mgrName}
                  onChange={(e) => setMgrName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Official Work Email</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="manager.westdeck@mall.com"
                  value={mgrEmail}
                  onChange={(e) => setMgrEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Assign Initial Password</label>
                <input
                  type="text"
                  className="form-input mono"
                  value={mgrPassword}
                  onChange={(e) => setMgrPassword(e.target.value)}
                  required
                  minLength={6}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Assign to Parking Facility</label>
                <select className="form-select" value={mgrLotId} onChange={(e) => setMgrLotId(e.target.value)} required>
                  {lots.map((l) => (
                    <option key={l.id} value={l.id}>{l.name} ({l.locationType})</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={() => setIsProvisionOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  Issue ID & Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: ADD FACILITY */}
      {isAddFacilityOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Register New Parking Facility</h2>
              <button className="btn btn-ghost" style={{ padding: '0.35rem' }} onClick={() => setIsAddFacilityOpen(false)}>
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateFacility}>
              <div className="form-group">
                <label className="form-label">Facility / Mall Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Apex Central Mall - North Parking"
                  value={facilityName}
                  onChange={(e) => setFacilityName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Location Domain</label>
                <select className="form-select" value={facilityType} onChange={(e) => setFacilityType(e.target.value)}>
                  <option value="MALL">Shopping Mall</option>
                  <option value="CAMPUS">University / College Campus</option>
                  <option value="COMPANY">Corporate Tech Park</option>
                  <option value="AIRPORT">Airport Terminal</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Street Address</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="400 Boulevard Way, Downtown"
                  value={facilityAddress}
                  onChange={(e) => setFacilityAddress(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Total Floors (Decks)</label>
                <input
                  type="number"
                  min="1"
                  max="10"
                  className="form-input"
                  value={facilityFloors}
                  onChange={(e) => setFacilityFloors(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={() => setIsAddFacilityOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-emerald" style={{ flex: 1 }}>
                  Create Facility
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
