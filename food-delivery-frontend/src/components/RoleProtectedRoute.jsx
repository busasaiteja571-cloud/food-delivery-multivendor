import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// Extends the idea from ProtectedRoute: not just "logged in," but
// "logged in AND holding one of the allowed roles for this screen."
function RoleProtectedRoute({ allowedRoles, children }) {
  const { user, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // includes() lets a screen allow MULTIPLE roles later (e.g. an admin
  // screen that both ADMIN and RESTAURANT_OWNER can see) without
  // rewriting this component.
  if (!allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default RoleProtectedRoute;