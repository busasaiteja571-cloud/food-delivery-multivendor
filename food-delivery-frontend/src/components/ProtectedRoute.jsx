import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// A component whose entire job is a conditional: render its children
// normally if logged in, or redirect to /login if not. Wrapping any
// page's <Route element={...}> in this instantly makes it protected.
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    // <Navigate> is the DECLARATIVE version of navigate('/login') —
    // used directly in JSX output rather than called imperatively
    // inside an event handler.
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;