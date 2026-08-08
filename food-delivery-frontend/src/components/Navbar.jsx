import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css'
import { useAuth } from '../context/AuthContext';
import NotificationBell from './NotificationBell.jsx';
function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }
  return (
    <nav className="navbar">
      {/* Link, not <a>, is essential: <a> triggers a full page reload,
          which would destroy all of React's in-memory state (like login
          status) on every navigation. Link intercepts the click and lets
          React Router swap the view instead. */}
      <Link to="/" className="navbar-brand">FoodDelivery</Link>
      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            {isAuthenticated && <NotificationBell />}
            {isAuthenticated && <Link to="/my-orders">My Orders</Link>}
            {user.role === 'ADMIN' && <Link to="/admin">Admin Dashboard</Link>}
            {user?.role === 'DELIVERY_AGENT' && <Link to="/delivery">Deliveries</Link>}
            {user?.role === 'RESTAURANT_OWNER' && <Link to="/dashboard">Dashboard</Link>}
            <span className="navbar-username">Hi, {user.fullName}</span>
            <button className="navbar-logout" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;