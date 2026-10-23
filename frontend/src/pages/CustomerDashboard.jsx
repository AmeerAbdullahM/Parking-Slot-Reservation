import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  Car, MapPin, Zap, Shield, Umbrella, Accessibility,
  Calendar, Clock, CheckCircle2, AlertTriangle, QrCode,
  Layers, Plus, Trash2, ArrowRight, DollarSign, X
} from 'lucide-react';

export default function CustomerDashboard({ activeTab, setActiveTab }) {
  const { user } = useAuth();

  // Lots & Map State
  const [lots, setLots] = useState([]);
  const [selectedLot, setSelectedLot] = useState(null);
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [slotMapData, setSlotMapData] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);

  // Booking Modal State
  const [isBookingOpen, setIsBookingOpen] = useState(false);
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [startTime, setStartTime] = useState('');
  const [durationHours, setDurationHours] = useState(2);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingError, setBookingError] = useState('');

  // Customer Passes & History State
  const [reservations, setReservations] = useState([]);
  const [passesLoading, setPassesLoading] = useState(false);

  // Check-Out / Penalty Receipt Modal
  const [penaltyReceipt, setPenaltyReceipt] = useState(null);

  // New Vehicle Modal State
  const [isAddVehicleOpen, setIsAddVehicleOpen] = useState(false);
  const [newPlate, setNewPlate] = useState('');
  const [newType, setNewType] = useState('SEDAN');
  const [newModel, setNewModel] = useState('');

  // Load lots & vehicles on mount
  useEffect(() => {
    loadLots();
    loadVehicles();
    loadReservations();

    // Default start time to now formatted for datetime-local
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    setStartTime(now.toISOString().slice(0, 16));
  }, []);

  // When selected lot or floor changes, fetch 2D slot map
  useEffect(() => {
    if (selectedLot) {
      loadSlotMap(selectedLot.id, selectedFloor);
    }
  }, [selectedLot, selectedFloor]);

  const loadLots = async () => {
    try {
      const data = await api.getLots();
      setLots(data || []);
      if (data && data.length > 0 && !selectedLot) {
        setSelectedLot(data[0]);
      }
    } catch (err) {
      console.error('Failed to load parking lots:', err);
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

  const loadVehicles = async () => {
    try {
      const data = await api.getMyVehicles();
      setVehicles(data || []);
      if (data && data.length > 0 && !selectedVehicleId) {
        setSelectedVehicleId(data[0].id);
      }
    } catch (err) {
      console.error('Failed to load user vehicles:', err);
    }
  };

  const loadReservations = async () => {
    setPassesLoading(true);
    try {
      const data = await api.getMyReservations(0, 20);
      setReservations(data?.content || []);
    } catch (err) {
      console.error('Failed to load reservations:', err);
    } finally {
      setPassesLoading(false);
    }
  };

  const handleSelectSlot = (slot) => {
    if (slot.status !== 'AVAILABLE') return;
    setSelectedSlot(slot);
    setBookingError('');
    setIsBookingOpen(true);
  };

  const handleCreateReservation = async (e) => {
    e.preventDefault();
    if (!selectedSlot || !selectedVehicleId || !startTime) {
      setBookingError('Please select a valid slot, vehicle, and start time.');
      return;
    }

    setBookingLoading(true);
    setBookingError('');

    try {
      const start = new Date(startTime);
      const end = new Date(start.getTime() + durationHours * 60 * 60 * 1000);

      // Format ISO string without milliseconds
      const formatIso = (d) => d.toISOString().split('.')[0];

      await api.createReservation({
        slotId: selectedSlot.id,
        vehicleId: selectedVehicleId,
        startTime: formatIso(start),
        endTime: formatIso(end)
      });

      setIsBookingOpen(false);
      setSelectedSlot(null);
      loadSlotMap(selectedLot.id, selectedFloor);
      loadReservations();
      setActiveTab('passes');
    } catch (err) {
      setBookingError(err.message || 'Booking conflict! Please choose another time or bay.');
    } finally {
      setBookingLoading(false);
    }
  };

  const handleCheckIn = async (resId) => {
    try {
      await api.checkIn(resId);
      loadReservations();
      if (selectedLot) loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert('Check-in error: ' + err.message);
    }
  };

  const handleCheckOut = async (resId) => {
    try {
      const result = await api.checkOut(resId);
      setPenaltyReceipt(result);
      loadReservations();
      if (selectedLot) loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert('Check-out error: ' + err.message);
    }
  };

  const handleCancel = async (resId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await api.cancelReservation(resId);
      loadReservations();
      if (selectedLot) loadSlotMap(selectedLot.id, selectedFloor);
    } catch (err) {
      alert('Cancellation error: ' + err.message);
    }
  };

  const handleAddVehicle = async (e) => {
    e.preventDefault();
    try {
      await api.registerVehicle({
        licensePlate: newPlate,
        vehicleType: newType,
        model: newModel
      });
      setIsAddVehicleOpen(false);
      setNewPlate('');
      setNewModel('');
      loadVehicles();
    } catch (err) {
      alert(err.message || 'Failed to register vehicle');
    }
  };

  const handleDeleteVehicle = async (id) => {
    if (!window.confirm('Delete this vehicle?')) return;
    try {
      await api.deleteVehicle(id);
      loadVehicles();
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>

      {/* VIEW 1: BROWSE FACILITIES & INTERACTIVE 2D BUS-STYLE MAP */}
      {(activeTab === 'browse' || activeTab === 'default') && (
        <div>
          {/* Mall / Facility Selector Header */}
          <div style={{ marginBottom: '2rem' }}>
            <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Explore Parking Facilities</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
              Select a shopping mall or university deck, choose your bay on the 2D layout, and reserve your slot.
            </p>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
              gap: '1.25rem',
              marginTop: '1.5rem'
            }}>
              {lots.map((lot) => {
                const isSelected = selectedLot?.id === lot.id;
                return (
                  <div
                    key={lot.id}
                    className="glass-panel"
                    style={{
                      padding: '1.25rem',
                      cursor: 'pointer',
                      border: isSelected ? '2px solid #3b82f6' : '1px solid var(--border-subtle)',
                      boxShadow: isSelected ? 'var(--shadow-glow-blue)' : 'none',
                      transition: 'all 0.2s ease'
                    }}
                    onClick={() => {
                      setSelectedLot(lot);
                      setSelectedFloor(1);
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <span className="badge badge-customer">{lot.locationType}</span>
                      <span style={{ fontSize: '0.8rem', color: '#10b981', fontWeight: 700 }}>
                        {lot.availableSlots} / {lot.totalSlots} Bays Free
                      </span>
                    </div>

                    <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginTop: '0.75rem' }}>{lot.name}</h3>
                    <p style={{ color: 'var(--text-dim)', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.35rem', marginTop: '0.35rem' }}>
                      <MapPin size={14} /> {lot.address}
                    </p>

                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '1rem' }}>
                      {lot.amenities?.map((a) => (
                        <span key={a.id} style={{
                          fontSize: '0.72rem',
                          background: 'rgba(255,255,255,0.05)',
                          padding: '0.2rem 0.5rem',
                          borderRadius: '6px',
                          color: 'var(--text-muted)'
                        }}>
                          {a.name}
                        </span>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Interactive 2D Visual Bay Selector Deck */}
          {selectedLot && (
            <div className="slot-deck">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
                <div>
                  <h2 style={{ fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Layers size={22} color="#3b82f6" />
                    <span>{selectedLot.name} — Floor Map</span>
                  </h2>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.2rem' }}>
                    Interactive 2D bay selector (like seat selection). Click an available bay to reserve.
                  </p>
                </div>

                {/* Floor Switcher Tabs */}
                <div style={{ display: 'flex', gap: '0.5rem', background: 'rgba(15,23,42,0.8)', padding: '0.35rem', borderRadius: '12px', border: '1px solid var(--border-subtle)' }}>
                  {slotMapData?.availableFloors?.map((fl) => (
                    <button
                      key={fl}
                      className={`btn ${selectedFloor === fl ? 'btn-primary' : 'btn-ghost'}`}
                      style={{ padding: '0.45rem 1rem', fontSize: '0.85rem' }}
                      onClick={() => setSelectedFloor(fl)}
                    >
                      {fl === 1 ? 'Floor 1 (B1)' : fl === 2 ? 'Floor 2 (Ground)' : `Floor ${fl} (Level ${fl - 1})`}
                    </button>
                  ))}
                </div>
              </div>

              {/* Status & Bay Legend */}
              <div style={{
                display: 'flex',
                gap: '1.5rem',
                flexWrap: 'wrap',
                background: 'rgba(15,23,42,0.6)',
                padding: '0.85rem 1.25rem',
                borderRadius: '12px',
                marginBottom: '1.5rem',
                fontSize: '0.82rem',
                border: '1px solid var(--border-subtle)'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div style={{ width: '14px', height: '14px', borderRadius: '4px', background: 'rgba(16, 185, 129, 0.4)', border: '1px solid #10b981' }} />
                  <span>Available (Clickable)</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div style={{ width: '14px', height: '14px', borderRadius: '4px', background: 'rgba(244, 63, 94, 0.4)', border: '1px solid #f43f5e' }} />
                  <span>Occupied / Reserved</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Zap size={15} color="#06b6d4" />
                  <span>EV Fast Charger</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Accessibility size={15} color="#a855f7" />
                  <span>Handicap Accessible</span>
                </div>
              </div>

              {/* Floor Bays Grid Layout */}
              <div className="slot-grid">
                {slotMapData?.slots?.map((slot) => {
                  const isAvail = slot.status === 'AVAILABLE';
                  const isEV = slot.slotType === 'EV_CHARGING';
                  const isHandicap = slot.slotType === 'HANDICAPPED';
                  const isVIP = slot.slotType === 'VIP';

                  return (
                    <div
                      key={slot.id}
                      className={`bay-slot ${isAvail ? 'bay-available' : 'bay-occupied'} ${isEV ? 'bay-ev' : ''}`}
                      onClick={() => handleSelectSlot(slot)}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span className="mono" style={{ fontWeight: 800, fontSize: '1rem' }}>
                          {slot.slotNumber}
                        </span>
                        {isEV && <Zap size={16} color="#06b6d4" title="EV Fast Charging" />}
                        {isHandicap && <Accessibility size={16} color="#a855f7" title="Handicap Accessible" />}
                        {isVIP && <span style={{ fontSize: '0.7rem', color: '#f59e0b', fontWeight: 800 }}>VIP</span>}
                      </div>

                      {/* Stall graphic divider */}
                      <div style={{
                        height: '42px',
                        border: '1px dashed rgba(255,255,255,0.1)',
                        borderRadius: '6px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '0.72rem',
                        color: isAvail ? 'rgba(52, 211, 153, 0.8)' : 'rgba(251, 113, 133, 0.8)'
                      }}>
                        {isAvail ? 'FREE' : 'OCCUPIED'}
                      </div>

                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem' }}>
                        <span style={{ color: 'var(--text-dim)' }}>Rate</span>
                        <span style={{ fontWeight: 700, color: 'var(--text-main)' }}>${slot.baseHourlyRate}/hr</span>
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="parking-road">
                <span>▲ CENTRAL DRIVEWAY — LANE ENTRANCE / EXIT ▲</span>
              </div>
            </div>
          )}
        </div>
      )}

      {/* VIEW 2: MY PASSES & ACTIVE BOOKINGS */}
      {activeTab === 'passes' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <div>
              <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>My Parking Passes & Receipts</h1>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
                Present your digital pass upon arrival. Overstay penalties calculate automatically upon departure.
              </p>
            </div>
          </div>

          {passesLoading ? (
            <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-dim)' }}>Loading passes...</div>
          ) : reservations.length === 0 ? (
            <div className="glass-panel" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
              <Car size={48} color="#64748b" style={{ margin: '0 auto 1rem' }} />
              <h3>No active parking reservations found</h3>
              <p style={{ color: 'var(--text-muted)', marginTop: '0.35rem', marginBottom: '1.5rem' }}>
                Explore parking facilities and book a bay using our 2D floor selector.
              </p>
              <button className="btn btn-primary" onClick={() => setActiveTab('browse')}>
                Browse Available Bays
              </button>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '1.5rem' }}>
              {reservations.map((res) => {
                const isConfirmed = res.status === 'CONFIRMED';
                const isCheckedIn = res.status === 'CHECKED_IN';
                const isCompleted = res.status === 'COMPLETED';
                const isOverstayed = res.status === 'OVERSTAYED';

                return (
                  <div key={res.id} className="glass-panel" style={{ padding: '1.5rem', position: 'relative' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <span className="mono" style={{ fontSize: '0.8rem', color: 'var(--text-dim)', letterSpacing: '0.05em' }}>
                          PASS CODE:
                        </span>
                        <div className="mono" style={{ fontSize: '1.1rem', fontWeight: 800, color: '#38bdf8' }}>
                          {res.reservationCode}
                        </div>
                      </div>

                      <span className={`badge ${
                        isCheckedIn ? 'badge-admin' : isConfirmed ? 'badge-customer' : isOverstayed ? 'badge-manager' : 'badge-customer'
                      }`}>
                        {res.status}
                      </span>
                    </div>

                    <div style={{ margin: '1.25rem 0', padding: '1rem', background: 'rgba(15,23,42,0.8)', borderRadius: '12px', border: '1px solid var(--border-subtle)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>Facility:</span>
                        <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>{res.lotName}</span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>Parking Bay:</span>
                        <span style={{ fontWeight: 800, color: '#10b981', fontSize: '1rem' }} className="mono">{res.slotNumber} (Floor {res.floorNumber})</span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>Vehicle Plate:</span>
                        <span style={{ fontWeight: 700 }} className="mono">{res.licensePlate}</span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>Booked Window:</span>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-main)' }}>
                          {new Date(res.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} — {new Date(res.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    </div>

                    {/* Fees Breakdown */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
                      <span style={{ color: 'var(--text-dim)', fontSize: '0.85rem' }}>Base Fee: ${res.baseFee}</span>
                      {res.penaltyFee > 0 && (
                        <span style={{ color: '#fb7185', fontSize: '0.85rem', fontWeight: 700 }}>
                          Penalty: +${res.penaltyFee}
                        </span>
                      )}
                      <span style={{ fontWeight: 800, fontSize: '1.1rem', color: '#34d399' }}>
                        Total: ${res.totalAmount}
                      </span>
                    </div>

                    {/* Action Controls */}
                    <div style={{ display: 'flex', gap: '0.65rem' }}>
                      {isConfirmed && (
                        <>
                          <button className="btn btn-emerald" style={{ flex: 1 }} onClick={() => handleCheckIn(res.id)}>
                            <CheckCircle2 size={16} /> Check-In
                          </button>
                          <button className="btn btn-ghost" onClick={() => handleCancel(res.id)}>
                            Cancel
                          </button>
                        </>
                      )}

                      {isCheckedIn && (
                        <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => handleCheckOut(res.id)}>
                          Check-Out & Pay (Exit)
                        </button>
                      )}

                      {(isCompleted || isOverstayed) && (
                        <div style={{ width: '100%', textAlign: 'center', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                          Completed on {new Date(res.actualCheckOut).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* VIEW 3: MY VEHICLES */}
      {activeTab === 'vehicles' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <div>
              <h1 style={{ fontSize: '1.85rem', fontWeight: 800 }}>Registered Vehicles</h1>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
                Add your license plates for seamless parking slot assignment.
              </p>
            </div>

            <button className="btn btn-primary" onClick={() => setIsAddVehicleOpen(true)}>
              <Plus size={16} /> Add Vehicle
            </button>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.25rem' }}>
            {vehicles.map((v) => (
              <div key={v.id} className="glass-panel" style={{ padding: '1.25rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span className="badge badge-customer">{v.vehicleType}</span>
                  <button
                    className="btn btn-ghost"
                    style={{ padding: '0.35rem', color: '#fb7185' }}
                    onClick={() => handleDeleteVehicle(v.id)}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>

                <div className="mono" style={{ fontSize: '1.35rem', fontWeight: 800, marginTop: '0.85rem', letterSpacing: '0.05em' }}>
                  {v.licensePlate}
                </div>
                <div style={{ color: 'var(--text-dim)', fontSize: '0.85rem', marginTop: '0.25rem' }}>
                  {v.model || 'Standard Vehicle'}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* MODAL 1: BOOKING CONFIRMATION MODAL */}
      {isBookingOpen && selectedSlot && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>
                Reserve Bay <span className="mono" style={{ color: '#38bdf8' }}>{selectedSlot.slotNumber}</span>
              </h2>
              <button className="btn btn-ghost" style={{ padding: '0.35rem' }} onClick={() => setIsBookingOpen(false)}>
                <X size={18} />
              </button>
            </div>

            {bookingError && (
              <div style={{
                padding: '0.75rem 1rem',
                background: 'rgba(244, 63, 94, 0.15)',
                border: '1px solid rgba(244, 63, 94, 0.3)',
                borderRadius: '8px',
                color: '#fb7185',
                fontSize: '0.85rem',
                marginBottom: '1rem'
              }}>
                {bookingError}
              </div>
            )}

            <form onSubmit={handleCreateReservation}>
              <div className="form-group">
                <label className="form-label">Select Your Vehicle</label>
                <select
                  className="form-select"
                  value={selectedVehicleId}
                  onChange={(e) => setSelectedVehicleId(e.target.value)}
                  required
                >
                  {vehicles.map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.licensePlate} ({v.model || v.vehicleType})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Reservation Start Date & Time</label>
                <input
                  type="datetime-local"
                  className="form-input"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Duration: {durationHours} Hour(s)</label>
                <input
                  type="range"
                  min="1"
                  max="12"
                  step="1"
                  value={durationHours}
                  onChange={(e) => setDurationHours(parseInt(e.target.value))}
                  style={{ width: '100%', accentColor: '#3b82f6' }}
                />
              </div>

              {/* Live Cost Estimate Preview */}
              <div style={{
                background: 'rgba(15, 23, 42, 0.8)',
                padding: '1rem',
                borderRadius: '12px',
                border: '1px solid var(--border-subtle)',
                marginBottom: '1.5rem'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  <span>Hourly Rate:</span>
                  <span>${selectedSlot.baseHourlyRate}/hr</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                  <span>Duration:</span>
                  <span>{durationHours} hour(s)</span>
                </div>
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  fontWeight: 800,
                  fontSize: '1.15rem',
                  color: '#34d399',
                  marginTop: '0.75rem',
                  paddingTop: '0.75rem',
                  borderTop: '1px solid rgba(255,255,255,0.1)'
                }}>
                  <span>Estimated Total:</span>
                  <span>${(selectedSlot.baseHourlyRate * durationHours).toFixed(2)}</span>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ flex: 1 }}
                  onClick={() => setIsBookingOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  style={{ flex: 2 }}
                  disabled={bookingLoading}
                >
                  {bookingLoading ? 'Reserving...' : 'Confirm Reservation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: OVERSTAY PENALTY & EXIT RECEIPT MODAL */}
      {penaltyReceipt && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ textAlign: 'center' }}>
            <div style={{
              display: 'inline-flex',
              padding: '1rem',
              borderRadius: '50%',
              background: penaltyReceipt.penaltyFee > 0 ? 'rgba(244, 63, 94, 0.15)' : 'rgba(16, 185, 129, 0.15)',
              color: penaltyReceipt.penaltyFee > 0 ? '#fb7185' : '#34d399',
              marginBottom: '1rem'
            }}>
              {penaltyReceipt.penaltyFee > 0 ? <AlertTriangle size={36} /> : <CheckCircle2 size={36} />}
            </div>

            <h2 style={{ fontSize: '1.35rem', fontWeight: 800 }}>
              {penaltyReceipt.penaltyFee > 0 ? 'Overstay Penalty Incurred!' : 'Departure Confirmed'}
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '0.35rem' }}>
              {penaltyReceipt.message}
            </p>

            <div style={{
              background: 'rgba(15, 23, 42, 0.8)',
              padding: '1.25rem',
              borderRadius: '12px',
              border: '1px solid var(--border-subtle)',
              margin: '1.5rem 0',
              textAlign: 'left'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-dim)' }}>Pass Code:</span>
                <span className="mono" style={{ fontWeight: 700 }}>{penaltyReceipt.reservationCode}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-dim)' }}>Overstay Minutes:</span>
                <span style={{ fontWeight: 700, color: penaltyReceipt.overstayMinutes > 15 ? '#fb7185' : 'var(--text-main)' }}>
                  {penaltyReceipt.overstayMinutes} min
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-dim)' }}>Penalty Surcharge:</span>
                <span style={{ fontWeight: 700, color: '#fb7185' }}>${penaltyReceipt.penaltyFee}</span>
              </div>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                fontWeight: 800,
                fontSize: '1.15rem',
                color: '#34d399',
                paddingTop: '0.75rem',
                borderTop: '1px solid rgba(255,255,255,0.1)'
              }}>
                <span>Total Amount Billed:</span>
                <span>${penaltyReceipt.totalAmount}</span>
              </div>
            </div>

            <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => setPenaltyReceipt(null)}>
              Dismiss Receipt
            </button>
          </div>
        </div>
      )}

      {/* MODAL 3: ADD VEHICLE MODAL */}
      {isAddVehicleOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Register New Vehicle</h2>
              <button className="btn btn-ghost" style={{ padding: '0.35rem' }} onClick={() => setIsAddVehicleOpen(false)}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleAddVehicle}>
              <div className="form-group">
                <label className="form-label">License Plate Number</label>
                <input
                  type="text"
                  className="form-input mono"
                  placeholder="e.g. ABC-1234"
                  value={newPlate}
                  onChange={(e) => setNewPlate(e.target.value.toUpperCase())}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Vehicle Type</label>
                <select className="form-select" value={newType} onChange={(e) => setNewType(e.target.value)}>
                  <option value="SEDAN">Sedan</option>
                  <option value="SUV">SUV (Wide Bay Preferred)</option>
                  <option value="EV">Electric Vehicle (EV Charger Compatible)</option>
                  <option value="COMPACT">Compact / Hatchback</option>
                  <option value="MOTORCYCLE">Motorcycle</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Make & Model (Optional)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Tesla Model Y / Honda Civic"
                  value={newModel}
                  onChange={(e) => setNewModel(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.5rem' }}>
                <button type="button" className="btn btn-ghost" style={{ flex: 1 }} onClick={() => setIsAddVehicleOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-emerald" style={{ flex: 1 }}>
                  Save Vehicle
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
