import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { restaurantService } from '../services/restaurantService.js';
import './RestaurantListPage.css';

function RestaurantListPage() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Consolidated fetch function that accepts an optional search query
  const fetchRestaurants = async (query = '') => {
    try {
      setLoading(true);
      setError('');
      let response;
      if (query) {
        response = await restaurantService.search(query);
      } else {
        response = await restaurantService.getAll();
      }
      setRestaurants(response.data);
    } catch (err) {
      setError('Could not load restaurants. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // setTimeout schedules the actual fetch to happen 400ms AFTER the
    // user stops typing — not on every single keystroke. If the user
    // types another character before 400ms passes, the cleanup function
    // below cancels the pending timer, and a new one starts.
    const timeoutId = setTimeout(() => {
      fetchRestaurants(searchTerm);
    }, 400);
    // Cleanup: runs before the NEXT effect execution (i.e., the next
    // keystroke) OR on unmount. This is what actually implements
    // "debouncing" — cancelling stale, no-longer-relevant timers.
    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  // useEffect with an empty dependency array ([]) means: run this function
  // exactly ONCE, right after the component first renders — never again
  // on subsequent re-renders, unlike code in the component body itself,
  // which would re-run on every single render.
  useEffect(() => {
    fetchRestaurants();
  }, []); // <-- the empty array IS the "only run once" instruction

  if (loading && restaurants.length === 0) {
    return <p className="status-message">Loading restaurants...</p>;
  }

  if (error && restaurants.length === 0) {
    return <p className="status-message error-message">{error}</p>;
  }

  return (
    <div className="restaurant-list-page">
      <h1>Restaurants near you</h1>
      <input
        type="text"
        className="search-input"
        placeholder="Search restaurants..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      {loading ? (
        <p className="status-message">Loading restaurants...</p>
      ) : error ? (
        <p className="status-message error-message">{error}</p>
      ) : restaurants.length === 0 ? (
        <p className="status-message">No restaurants found.</p>
      ) : (
        <div className="restaurant-grid">
          {/* .map() turns the array of restaurant objects into an array of
              JSX cards — React renders each one. `key` must be unique and
              stable per item so React can efficiently track which card is
              which across re-renders, without re-creating all of them. */}
          {restaurants.map((restaurant) => (
            <Link
              key={restaurant.restaurantId}
              to={`/restaurants/${restaurant.restaurantId}`}
              // Adds a CSS class when closed, letting us visually dim/grey it
              // out without removing it from the list entirely.
              className={`restaurant-card ${!restaurant.isActive ? 'restaurant-closed' : ''}`}
            >
              <div className="restaurant-card-header">
                <h2>{restaurant.name}</h2>
                <span className={`status-dot ${restaurant.isActive ? 'open' : 'closed'}`}>
                  {restaurant.isActive ? 'Open' : 'Closed'}
                </span>
              </div>
              <p className="restaurant-address">{restaurant.address}</p>
              {restaurant.description && (
                <p className="restaurant-description">{restaurant.description}</p>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}

export default RestaurantListPage;