import axiosInstance from '../api/axiosInstance.js';

export const restaurantService = {
  getAll: () => axiosInstance.get('/restaurants'),
  search: (query) => axiosInstance.get('/restaurants/search', { params: { query } }),
  getMine: () => axiosInstance.get('/restaurants/mine'),
  create: (data) => axiosInstance.post('/restaurants', data),
  update: (id, data) => axiosInstance.put(`/restaurants/${id}`, data),
  toggleStatus: (id) => axiosInstance.patch(`/restaurants/${id}/toggle-status`),
};