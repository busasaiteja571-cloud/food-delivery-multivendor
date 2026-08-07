import axiosInstance from '../api/axiosInstance.js';

export const menuItemService = {
  getForRestaurant: (restaurantId) =>
    axiosInstance.get(`/restaurants/${restaurantId}/menu-items`),
  add: (restaurantId, data) =>
    axiosInstance.post(`/restaurants/${restaurantId}/menu-items`, data),
  update: (restaurantId, itemId, data) =>
    axiosInstance.put(`/restaurants/${restaurantId}/menu-items/${itemId}`, data),
};