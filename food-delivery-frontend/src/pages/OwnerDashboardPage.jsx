import { useState, useEffect } from 'react';
import { restaurantService } from '../services/restaurantService.js';
import CreateRestaurantForm from '../components/CreateRestaurantForm';
import MenuManager from '../components/MenuManager.jsx';
import RestaurantOrders from '../components/RestaurantOrders.jsx';
import './OwnerDashboardPage.css'

function OwnerDashboardPage() {
  const [restaurant, setRestaurant] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMyRestaurant();
  }, []);

  async function fetchMyRestaurant() {
    setLoading(true);
    try {
      const response = await restaurantService.getMine();
      // Our schema allows an owner to have multiple restaurants, but this
      // dashboard, for now, manages just the first one — a deliberate
      // scope decision, not an oversight; multi-restaurant switching
      // would be a reasonable future addition.
      setRestaurant(response.data.length > 0 ? response.data[0] : null);
    } catch (err) {
      setRestaurant(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleToggleStatus() {
    try {
      await restaurantService.toggleStatus(restaurant.restaurantId);
      fetchMyRestaurant(); // refresh to reflect the new status immediately
    } catch (err) {
      console.error("Failed to toggle restaurant status", err);
    }
  }

  if (loading) return <p className="status-message">Loading dashboard...</p>;

  return (
    <div className="dashboard-page">
      <h1>Restaurant Dashboard</h1>

      {!restaurant ? (
        // onCreated is a callback passed DOWN into the child form — when
        // the form successfully creates a restaurant, it calls this to
        // tell the PARENT to refresh, rather than the child managing
        // the parent's state directly.
        <CreateRestaurantForm onCreated={fetchMyRestaurant} />
      ) : (
        <>
          <div className="restaurant-summary-card">
            <h2>{restaurant.name}</h2>
            <p>{restaurant.address}</p>
            <span className={`status-badge ${restaurant.isActive ? 'active' : 'inactive'}`}>
              {restaurant.isActive ? 'Active' : 'Inactive'}
            </span>
            <button className="toggle-status-btn" onClick={handleToggleStatus}>
              {restaurant.isActive ? 'Close Shop' : 'Open Shop'}
            </button>
          </div>
          <MenuManager restaurantId={restaurant.restaurantId} />
          <RestaurantOrders restaurantId={restaurant.restaurantId} />
        </>
      )}
    </div>
  );
}

export default OwnerDashboardPage;