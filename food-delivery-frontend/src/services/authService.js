import axiosInstance from '../api/axiosInstance.js';

// Each function here is ONE clearly-named operation. Components call
// authService.login(...) instead of remembering the exact URL, method,
// and payload shape themselves — that knowledge lives in exactly one place.
export const authService = {
  register: (formData) => axiosInstance.post('/auth/register', formData),
  login: (formData) => axiosInstance.post('/auth/login', formData),
};