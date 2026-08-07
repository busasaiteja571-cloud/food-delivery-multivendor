import axiosInstance from '../api/axiosInstance.js';

export const notificationService = {
  getNotifications: (since) => axiosInstance.get('/notifications', { params: { since } }),
};
