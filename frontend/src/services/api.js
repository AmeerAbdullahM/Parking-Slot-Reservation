// Centralized API client for ParkPulse Smart Parking System

const BASE_URL = 'http://localhost:8080/api';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

const handleResponse = async (response) => {
  if (response.status === 204) {
    return null;
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `HTTP error! Status: ${response.status}`;
    throw new Error(errorMsg);
  }
  return data;
};

export const api = {
  // Auth
  login: async (email, password) => {
    const res = await fetch(`${BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return handleResponse(res);
  },

  register: async (fullName, email, password, phone) => {
    const res = await fetch(`${BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, password, phone })
    });
    return handleResponse(res);
  },

  getCurrentUser: async () => {
    const res = await fetch(`${BASE_URL}/auth/me`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Parking Lots
  getLots: async () => {
    const res = await fetch(`${BASE_URL}/lots`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  getLotById: async (id) => {
    const res = await fetch(`${BASE_URL}/lots/${id}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  getSlotMap: async (lotId, floor = 1) => {
    const res = await fetch(`${BASE_URL}/lots/${lotId}/slots/map?floor=${floor}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  createLot: async (lotData) => {
    const res = await fetch(`${BASE_URL}/lots`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(lotData)
    });
    return handleResponse(res);
  },

  addFloor: async (lotId) => {
    const res = await fetch(`${BASE_URL}/lots/${lotId}/floors`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Slots
  searchSlots: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const res = await fetch(`${BASE_URL}/slots?${query}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  createSlot: async (slotData) => {
    const res = await fetch(`${BASE_URL}/slots`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(slotData)
    });
    return handleResponse(res);
  },

  updateSlotStatus: async (slotId, status) => {
    const res = await fetch(`${BASE_URL}/slots/${slotId}/status?status=${status}`, {
      method: 'PATCH',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  deleteSlot: async (slotId) => {
    const res = await fetch(`${BASE_URL}/slots/${slotId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Vehicles
  getMyVehicles: async () => {
    const res = await fetch(`${BASE_URL}/vehicles/my`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  registerVehicle: async (vehicleData) => {
    const res = await fetch(`${BASE_URL}/vehicles`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(vehicleData)
    });
    return handleResponse(res);
  },

  deleteVehicle: async (vehicleId) => {
    const res = await fetch(`${BASE_URL}/vehicles/${vehicleId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Reservations
  createReservation: async (reservationData) => {
    const res = await fetch(`${BASE_URL}/reservations`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(reservationData)
    });
    return handleResponse(res);
  },

  getMyReservations: async (page = 0, size = 10) => {
    const res = await fetch(`${BASE_URL}/reservations/my?page=${page}&size=${size}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  getLotReservations: async (lotId, page = 0, size = 10) => {
    const res = await fetch(`${BASE_URL}/reservations/lot/${lotId}?page=${page}&size=${size}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  searchReservations: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const res = await fetch(`${BASE_URL}/reservations/search?${query}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  checkIn: async (reservationId) => {
    const res = await fetch(`${BASE_URL}/reservations/${reservationId}/check-in`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  checkOut: async (reservationId) => {
    const res = await fetch(`${BASE_URL}/reservations/${reservationId}/check-out`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  cancelReservation: async (reservationId) => {
    const res = await fetch(`${BASE_URL}/reservations/${reservationId}/cancel`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  // Admin
  createManager: async (managerData) => {
    const res = await fetch(`${BASE_URL}/admin/managers`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(managerData)
    });
    return handleResponse(res);
  },

  getAllManagers: async () => {
    const res = await fetch(`${BASE_URL}/admin/managers`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  getAdminStats: async () => {
    const res = await fetch(`${BASE_URL}/admin/stats`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  },

  getManagerStats: async (managerId) => {
    const res = await fetch(`${BASE_URL}/admin/manager-stats/${managerId}`, {
      headers: getAuthHeaders()
    });
    return handleResponse(res);
  }
};
