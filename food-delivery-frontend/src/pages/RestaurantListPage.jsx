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

  // useEffect for handling debounced search, but skipped on initial mount
  useEffect(() => {
    // Skip the debounce fetch on initial load since the empty dependency array effect handles it
    if (searchTerm === '') return;

    const timeoutId = setTimeout(() => {
      fetchRestaurants(searchTerm);
    }, 400);

    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  // useEffect with an empty dependency array ([]) means: run this function
  // exactly ONCE, right after the component first renders.
  useEffect(() => {
    fetchRestaurants();
  }, []);

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
          {restaurants.map((restaurant) => (
            <Link
              key={restaurant.restaurantId}
              to={`/restaurants/${restaurant.restaurantId}`}
              className={`restaurant-card ${!restaurant.isActive ? 'restaurant-closed' : ''}`}
            >
              {restaurant.imageUrl && (
                <img
                  src={restaurant.imageUrl}
                  alt={restaurant.name}
                  className="restaurant-card-img"
                  onError={(e) => { e.target.style.display = 'none'; }}
                />
              )}
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