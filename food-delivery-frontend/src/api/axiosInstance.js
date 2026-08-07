import axios from 'axios';

// A single, shared Axios instance instead of importing raw axios
// everywhere. This means the base URL (and, soon, auth headers) only
// need to be configured in ONE place, not repeated in every API call.
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// An Axios interceptor runs automatically before EVERY request this
// instance sends. Instead of manually adding an Authorization header
// to every single API call in every page, we do it here, exactly once.
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// A RESPONSE interceptor runs on every response (success or error) that
// comes BACK from any request made through this instance — the mirror
// image of the request interceptor above.
axiosInstance.interceptors.response.use(
  (response) => response, // successful responses pass through unchanged
  (error) => {
    // A 401 specifically means "your credentials are invalid or expired" —
    // distinct from a 403 (valid credentials, just not allowed to do
    // THIS), which individual pages should keep handling themselves,
    // since a 403 is a legitimate business-logic outcome, not a broken session.
    if(error.response?.status === 401){
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      // A hard redirect (not React Router's navigate) is intentional
      // here: this code lives OUTSIDE any component, so React Router's
      // hooks aren't available. A full page reload to /login also has
      // the side effect of fully resetting React's in-memory state,
      // which is exactly appropriate for an invalidated session.
      window.location.href = '/login';
    }
    // Re-throw so the ORIGINAL calling code's own .catch() still runs
    // too — this interceptor adds a global side effect, it doesn't
    // swallow the error or prevent normal error handling downstream.
    return Promise.reject(error);
  }
);

export default axiosInstance;