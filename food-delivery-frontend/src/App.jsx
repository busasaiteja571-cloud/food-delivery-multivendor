import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import RestaurantListPage from './pages/RestaurantListPage.jsx';
import Navbar from './components/Navbar.jsx';
import RestaurantMenuPage from './pages/RestaurantMenuPage.jsx';
import CheckoutPage from './pages/CheckoutPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import OwnerDashboardPage from './pages/OwnerDashboardPage.jsx';
import RoleProtectedRoute from './components/RoleProtectedRoute.jsx';
import DeliveryDashboardPage from './pages/DeliveryDashboardPage.jsx';
import AdminDashboardPage from './pages/AdminDashboardPage.jsx';
import MyOrdersPage from './pages/MyOrdersPage.jsx';

function App() {
  return (
    // BrowserRouter enables client-side routing: React intercepts URL
    // changes and swaps components in-place, instead of the browser
    // making a full new request to the server for every page.
    <BrowserRouter>
      <Navbar />
      <Routes>
        {/* Each Route maps a URL path to the component that renders there */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<RestaurantListPage />} />
        <Route path="/restaurants/:restaurantId" element={<RestaurantMenuPage />} />
        <Route path="/checkout" element={
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
          }
        />
        <Route path="/dashboard" element={
          <RoleProtectedRoute allowedRoles={['RESTAURANT_OWNER']}>
            <OwnerDashboardPage />
          </RoleProtectedRoute>
          }
        />
        <Route path="/delivery" element={
          <RoleProtectedRoute allowedRoles={['DELIVERY_AGENT']}>
            <DeliveryDashboardPage />
          </RoleProtectedRoute>
          }
        />
        <Route path="/admin" element={
          <RoleProtectedRoute allowedRoles={['ADMIN']}>
            <AdminDashboardPage />
          </RoleProtectedRoute>
          }
        />
        <Route path="/my-orders" element={
          <ProtectedRoute>
            <MyOrdersPage />
          </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;