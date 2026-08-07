import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService.js';
import './LoginPage.css';
function RegisterPage() {
  // A single state object holding all form fields, rather than 5
  // separate useState calls — keeps related data grouped together and
  // makes the update handler reusable across every input.
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'CUSTOMER',
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // useNavigate gives us a function to programmatically change routes —
  // used here to redirect to /login after a successful registration.
  const navigate = useNavigate();

  // One generic handler for every input field. It reads the input's
  // `name` attribute to know WHICH field in formData to update, so we
  // don't need a separate handler function per field.
  function handleChange(e) {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault(); // stops the browser's default full-page form submit
    setError('');
    setLoading(true);

    try {
      await authService.register(formData);
      navigate('/login'); // registration succeeded — send them to log in
    } catch (err) {
      // Our GlobalExceptionHandler (Phase 1) returns { error: "..." } —
      // we read that exact shape here to show the real backend message.
      const message = err.response?.data?.error || 'Registration failed. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h1>Create an account</h1>

        {/* Conditionally rendered — only appears in the DOM at all if
            error is a non-empty string */}
        {error && <p className="error-message">{error}</p>}

        <label>
          Full Name
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            required
          />
        </label>

        <label>
          Email
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </label>

        <label>
          Password
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            minLength={8}
          />
        </label>

        <label>
          Phone Number
          <input
            type="tel"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
          />
        </label>

        <label>
          I am a...
          <select name="role" value={formData.role} onChange={handleChange}>
            <option value="CUSTOMER">Customer</option>
            <option value="RESTAURANT_OWNER">Restaurant Owner</option>
            <option value="DELIVERY_AGENT">Delivery Agent</option>
          </select>
        </label>

        <button type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Register'}
        </button>

        <p>Already have an account? <Link to="/login">Log in</Link></p>
      </form>
    </div>
  );
}

export default RegisterPage;