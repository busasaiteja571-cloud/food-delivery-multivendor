import axiosInstance from '../api/axiosInstance.js';

export const orderService = {
  place: (restaurantId, data) =>
    axiosInstance.post(`/restaurants/${restaurantId}/orders`, data),
  getForRestaurant: (restaurantId) =>
    axiosInstance.get(`/restaurants/${restaurantId}/orders`),
  markPreparing: (restaurantId, orderId) =>
    axiosInstance.patch(`/restaurants/${restaurantId}/orders/${orderId}/prepare`),
  getMyOrders: () => axiosInstance.get('/customers/orders'),
  getAvailableDeliveries: () => axiosInstance.get('/orders/available'),
  getMyDeliveries: () => axiosInstance.get('/orders/mine'),
  claim: (orderId) => axiosInstance.patch(`/orders/${orderId}/claim`),
  deliver: (orderId) => axiosInstance.patch(`/orders/${orderId}/deliver`),
};