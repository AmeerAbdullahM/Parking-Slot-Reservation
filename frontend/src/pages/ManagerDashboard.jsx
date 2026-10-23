import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  Layers, Plus, Trash2, CheckCircle, AlertTriangle, Zap,
  Clock, DollarSign, Eye, RefreshCw, X, Shield, ArrowUpRight
} from 'lucide-react';

export default function ManagerDashboard({ activeTab }) {
  const { user } = useAuth();

  const [managedLots, setManagedLots] = useState([]);
  const [selectedLot, setSelectedLot] = useState(null);
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [slotMapData, setSlotMapData] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);

  // New Slot Modal State
  const [isAddSlotOpen, setIsAddSlotOpen] = useState(false);
  const [newSlotNumber, setNewSlotNumber] = useState('');
  const [newRow, setNewRow] = useState(1);
  const [newCol, setNewCol] = useState(1);
  const [newType, setNewType] = useState('REGULAR');
  const [newRate, setNewRate] = useState(5.0);
  const [slotError, setSlotError] = useState('');

  // Overstay penalty modal
  const [penaltyReceipt, setPenaltyReceipt] = useState(null);

  useEffect(() => {
    loadManagerData();
  }, [user]);

  useEffect(() => {
    if (selectedLot) {
      loadSlotMap(selectedLot.id, selectedFloor);
      loadLotBookings(selectedLot.id);
    }
  }, [selectedLot, selectedFloor]);

  const loadManagerData = async () => {
    setLoading(true);
    try {
      // Get all active lots
      const allLots = await api.getLots();
      setManagedLots(allLots || []);
      if (allLots && allLots.length > 0) {
        // If manager has an assigned lot id, select it, otherwise default to first
        const matched = allLots.find((l) => l.id === user.assignedLotId) || allLots[0];
        setSelectedLot(matched);
      }
    } catch (err) {
      console.error('Failed to load manager data:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadSlotMap = async (lotId, floor) => {
    try {
      const data = await api.getSlotMap(lotId, floor);
      setSlotMapData(data);
    } catch (err) {
      console.error('Failed to load slot map:', err);
    }
  };

  const loadLotBookings = async (lotId) => {
    try {
      const data = await api.getLotReservations(lotId, 0, 50);
      setBookings(data?.content || []);
    } catch (err) {
      console.error('Failed to load lot bookings:', err);
    }
  };

  const handleAddFloor = async () => {
    if (!selectedLot) return;
    try {
      const updated = await api.addFloor(selectedLot.id);
      setSelectedLot(updated);
      setSelectedFloor(updated.totalFloors);
      loadSlotMap(updated.id, updated.totalFloors);
      alert(`Floor ${updated.totalFloors} added successfully! You can now add parking bays to this floor.`);
    } catch (err) {
      alert(err.message || 'Failed to add floor');
    }
  };

  const handleCreateSlot = async (e) => {
    e.preventDefault();
    setSlotError('');
    try {
      await api.createSlot({
        slotNumber: newSlotNumber.trim(),
        floorNumber: selectedFloor,
        rowNumber: parseInt(newRow),
        colNumber: parseInt(newCol),
        slotType: newType,
        baseHourlyRate: parseFloat(newRate),
        parkingLotId: selectedLot.id
      });
      setIsAddSlotOpen(false);
      setNewSlotNumber('');
      loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      setSlotError(err.message || 'Failed to create slot');
    }
  };

  const handleToggleMaintenance = async (slotId, currentStatus) => {
    const nextStatus = currentStatus === 'MAINTENANCE' ? 'AVAILABLE' : 'MAINTENANCE';
    try {
      await api.updateSlotStatus(slotId, nextStatus);
      loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDeleteSlot = async (slotId) => {
    if (!window.confirm('Delete this parking bay?')) return;
    try {
      await api.deleteSlot(slotId);
      loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleCheckIn = async (resId) => {
    try {
      await api.checkIn(resId);
      loadLotBookings(selectedLot.id);
      loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert(err.message);
    }
  };

  const handleCheckOut = async (resId) => {
    try {
      const result = await api.checkOut(resId);
      setPenaltyReceipt(result);
      loadLotBookings(selectedLot.id);
      loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>

      {/* Header with facility summary */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <span className="badge badge-manager">Facility Manager Console</span>
            <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>• {user?.fullName}</span>
          </div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '0.35rem' }}>
            {selectedLot ? selectedLot.name : 'Mall Parking Operations'}
          </h1>
        </div>

        {/* Facility Selector */}
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select
            className="form-select"
            style={{ width: 'auto', minWidth: '220px' }}
            value={selectedLot?.id || ''}
            onChange={(e) => {
              const lot = managedLots.find((l) => l.id === parseInt(e.target.value));
              if (lot) {
                setSelectedLot(lot);
                setSelectedFloor(1);
              }
            }}
          >
            {managedLots.map((l) => (
              <option key={l.id} value={l.id}>{l.name}</option>
            ))}
          </select>

          <button className="btn btn-ghost" onClick={() => loadSlotMap(selectedLot.id, selectedFloor)} title="Refresh">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>

      {/* TAB 1: 2D BAY BUILDER & FLOOR MANAGER */}
      {(activeTab === 'manager-map' || activeTab === 'default') && selectedLot && (
        <div>
          {/* Controls bar: Floor selector + Add Floor + Add Slot buttons */}
          <div className="glass-panel" style={{ padding: '1.25rem', marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <span style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-muted)' }}>Floors:</span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                {slotMapData?.availableFloors?.map((fl) => (
                  <button
                    key={fl}
                    className={`btn ${selectedFloor === fl ? 'btn-primary' : 'btn-ghost'}`}
                    style={{ padding: '0.4rem 0.85rem', fontSize: '0.82rem' }}
                    onClick={() => setSelectedFloor(fl)}
                  >
                    {fl === 1 ? 'Floor 1 (B1)' : fl === 2 ? 'Floor 2 (G)' : `Floor ${fl}`}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem' }}>
              <button className="btn btn-ghost" onClick={handleAddFloor}>
                <Plus size={16} /> Add New Floor
              </button>
              <button className="btn btn-emerald" onClick={() => setIsAddSlotOpen(true)}>
                <Plus size={16} /> Add Parking Bay
              </button>
            </div>
          </div>

          {/* Interactive 2D Visual Bay Grid */}
          <div className="slot-deck">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>
                  Floor {selectedFloor} Grid — {slotMapData?.availableSlotsOnFloor || 0} / {slotMapData?.totalSlotsOnFloor || 0} Available
                </h3>
                <p style={{ color: 'var(--text-dim)', fontSize: '0.82rem' }}>
                  Click "Toggle" on any bay to switch between Active and Maintenance mode.
                </p>
              </div>
            </div>

            <div className="slot-grid">
              {slotMapData?.slots?.map((slot) => {
                const isAvail = slot.status === 'AVAILABLE';
                const isOcc = slot.status === 'OCCUPIED';
                const isMaint = slot.status === 'MAINTENANCE';

                return (
                  <div
                    key={slot.id}
                    className={`bay-slot ${isAvail ? 'bay-available' : isOcc ? 'bay-occupied' : 'bay-maintenance'}`}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span className="mono" style={{ fontWeight: 800 }}>{slot.slotNumber}</span>
                      {slot.slotType === 'EV_CHARGING' && <Zap size={15} color="#06b6d4" />}
                    </div>

                    <div style={{ fontSize: '0.75rem', textAlign: 'center', margin: '0.35rem 0' }}>
                      <span style={{ fontWeight: 700 }}>{slot.status}</span>
                      <div style={{ color: 'var(--text-dim)', fontSize: '0.7rem' }}>${slot.baseHourlyRate}/hr</div>
                    </div>

                    <div style={{ display: 'flex', gap: '0.35rem' }}>
                      <button
                        type="button"
                        style={{
                          flex: 1,
                          padding: '0.25rem',
                          background: 'rgba(255,255,255,0.08)',
                          border: 'none',
                          borderRadius: '4px',
                          color: 'var(--text-muted)',
                          fontSize: '0.7rem',
                          cursor: 'pointer'
                        }}
                        onClick={() => handleToggleMaintenance(slot.id, slot.status)}
                      >
                        {isMaint ? 'Enable' : 'Hold'}
                      </button>
                      <button
                        type="button"
                        style={{
                          padding: '0.25rem 0.4rem',
                          background: 'rgba(244,63,94,0.15)',
                          border: 'none',
                          borderRadius: '4px',
                          color: '#fb7185',
                          cursor: 'pointer'
                        }}
                        onClick={() => handleDeleteSlot(slot.id)}
                      >
                        <Trash2 size={13} />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="parking-road">
              <span>▲ DRIVEWAY PASSAGE — ONE WAY TRAFFIC ▲</span>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: ARRIVALS & CHECK-IN DESK */}
      {activeTab === 'manager-bookings' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <div>
              <h2 style={{ fontSize: '1.45rem', fontWeight: 800 }}>Arrivals, Check-In & Departure Desk</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Verify incoming drivers, confirm bay occupancy, and collect overstay penalties upon departure.
              </p>
            </div>
          </div>

          <div className="glass-panel" style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-dim)' }}>
                  <th style={{ padding: '1rem' }}>Pass Code</th>
                  <th style={{ padding: '1rem' }}>Customer</th>
                  <th style={{ padding: '1rem' }}>Bay & Plate</th>
                  <th style={{ padding: '1rem' }}>Booked Time Window</th>
                  <th style={{ padding: '1rem' }}>Status</th>
                  <th style={{ padding: '1rem' }}>Total Fee</th>
                  <th style={{ padding: '1rem' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td className="mono" style={{ padding: '1rem', fontWeight: 700, color: '#38bdf8' }}>
                      {b.reservationCode}
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ fontWeight: 600 }}>{b.userFullName}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-dim)' }}>{b.userPhone || b.userEmail}</div>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span className="mono" style={{ fontWeight: 700, color: '#10b981' }}>{b.slotNumber}</span>
                      <div className="mono" style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{b.licensePlate}</div>
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.8rem' }}>
                      {new Date(b.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} — {new Date(b.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span className={`badge ${
                        b.status === 'CHECKED_IN' ? 'badge-admin' : b.status === 'CONFIRMED' ? 'badge-customer' : 'badge-manager'
                      }`}>
                        {b.status}
                      </span>
                    </td>
                    <td style={{ padding: '1rem', fontWeight: 700 }}>
                      ${b.totalAmount}
                      {b.penaltyFee > 0 && <span style={{ color: '#fb7185', fontSize: '0.75rem', display: 'block' }}>+${b.penaltyFee} penalty</span>}
                    </td>
                    <td style={{ padding: '1rem' }}>
                      {b.status === 'CONFIRMED' && (
                        <button className="btn btn-emerald" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }} onClick={() => handleCheckIn(b.id)}>
                          Check-In
                        </button>
                      )}
                      {b.status === 'CHECKED_IN' && (
                        <button className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }} onClick={() => handleCheckOut(b.id)}>
                          Check-Out
                        </button>
                      )}
                      {b.status !== 'CONFIRMED' && b.status !== 'CHECKED_IN' && (
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.8rem' }}>Settled</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* MODAL: ADD PARKING BAY TO 2D GRID */}
      {isAddSlotOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>
                Add Parking Bay to Floor {selectedFloor}
              </h2>
              <button className="btn btn-ghost" style={{ padding: '0.35rem' }} onClick={() => setIsAddSlotOpen(false)}>
                <X size={18} />
              </button>
            </div>

            {slotError && (
              <div style={{ padding: '0.75rem 1rem', background: 'rgba(244,63,94,0.15)', color: '#fb7185', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem' }}>
                {slotError}
              </div>
            )}

            <form onSubmit={handleCreateSlot}>
              <div className="form-group">
                <label className="form-label">Bay Identifier (Slot Number)</label>
                <input
                  type="text"
                  className="form-input mono"
                  placeholder="e.g. B1-13 or G-25"
                  value={newSlotNumber}
                  onChange={(e) => setNewSlotNumber(e.target.value.toUpperCase())}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Grid Row (1-5)</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    className="form-input"
                    value={newRow}
                    onChange={(e) => setNewRow(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Grid Column (1-10)</label>
                  <input
                    type="number"
                    min="1"
                    max="20"
                    className="form-input"
                    value={newCol}
                    onChange={(e) => setNewCol(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Bay Classification</label>
                <select className="form-select" value={newType} onChange={(e) => setNewType(e.target.value)}>
                  <option value="REGULAR">Regular Vehicle Bay</option>
                  <option value="EV_CHARGING">EV Fast Charging Station</option>
                  <option value="HANDICAPPED">Handicap Accessible Bay</option>
                  <option value="VIP">VIP Reserved Bay</option>
                  <option value="COMPACT">Compact Vehicle Only</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Base Hourly Rate ($/hr)</label>
                <input
                  type="number"
                  step="0.5"
                  min="0"
                  className="form-input"
                  value={newRate}
                  onChange={(e) => setNewRate(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={() => setIsAddSlotOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-emerald" style={{ flex: 1 }}>
                  Create Bay
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* OVERSTAY PENALTY RECEIPT MODAL */}
      {penaltyReceipt && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ textAlign: 'center' }}>
            <h2 style={{ fontSize: '1.3rem', fontWeight: 800 }}>Departure & Billing Settled</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: '0.5rem 0 1.5rem' }}>
              {penaltyReceipt.message}
            </p>
            <div style={{ background: 'rgba(15,23,42,0.8)', padding: '1rem', borderRadius: '12px', textAlign: 'left', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span>Pass Code:</span>
                <span className="mono" style={{ fontWeight: 700 }}>{penaltyReceipt.reservationCode}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span>Overstay Minutes:</span>
                <span style={{ fontWeight: 700, color: penaltyReceipt.overstayMinutes > 15 ? '#fb7185' : 'var(--text-main)' }}>
                  {penaltyReceipt.overstayMinutes} min
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span>Penalty Surcharge:</span>
                <span style={{ fontWeight: 700, color: '#fb7185' }}>${penaltyReceipt.penaltyFee}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '1.1rem', color: '#34d399', paddingTop: '0.5rem', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                <span>Total Collected:</span>
                <span>${penaltyReceipt.totalAmount}</span>
              </div>
            </div>
            <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => setPenaltyReceipt(null)}>
              Done
            </button>
          </div>
        </div>
      )}

    </div>
  );
}
