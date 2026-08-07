import axiosInstance from '../api/axiosInstance.js';

export const adminService = {
  getUsers: () => axiosInstance.get('/admin/users'),
  getRestaurants: () => axiosInstance.get('/admin/restaurants'),
  toggleRestaurantStatus: (id) =>
    axiosInstance.patch(`/admin/restaurants/${id}/toggle-status`),
  getOrders: () => axiosInstance.get('/admin/orders'),
};