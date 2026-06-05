import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({ baseURL: API_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const auth = {
  riderSignup: (data) => api.post('/api/users/signup', data),
  riderLogin: (data) => api.post('/api/users/login', data),
  driverSignup: (data) => api.post('/api/driver/auth/signup', data),
  driverLogin: (data) => api.post('/api/driver/auth/login', data),
  adminLogin: (data) => api.post('/api/admin/login', data),
};

export const rides = {
  getFare: (params) => api.get('/api/fare/calculate', { params }),
  book: (data) => api.post('/api/riders/book', data),
  cancel: () => api.put('/api/riders/cancel'),
  pendingDuties: () => api.get('/api/duties/getAllDutyDyStatusPending'),
  accept: (dutyId) => api.post(`/api/drivers/accept/${dutyId}`),
  complete: (dutyId) => api.post(`/api/drivers/complete/${dutyId}`),
  updateLocation: (data) => api.put('/api/drivers/location', data),
  goOnline: (data) => api.post('/api/drivers/online', data),
  goOffline: () => api.post('/api/drivers/offline'),
  nearbyDrivers: (lat, lng) => api.get('/api/drivers/nearby', { params: { lat, lng, radiusKm: 10 } }),
  getDuty: (dutyId) => api.get(`/api/duties/${dutyId}`),
};

export const payments = {
  create: (dutyId) => api.post(`/api/payments/create/${dutyId}`),
  confirm: (dutyId) => api.post('/api/payments/confirm', { dutyId }),
};

export const ratings = {
  submit: (data) => api.post('/api/ratings', data),
  driverSummary: (driverId) => api.get(`/api/drivers/${driverId}/rating`),
};

export const admin = {
  stats: () => api.get('/api/admin/stats'),
};

export default api;
