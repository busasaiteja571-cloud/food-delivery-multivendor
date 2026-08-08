import { useState, useEffect } from 'react';
import { menuItemService } from '../services/menuItemService.js';
import './MenuManager.css'
// restaurantId arrives as a PROP from OwnerDashboardPage — this component
// doesn't know or care how that ID was discovered, keeping it reusable.
function MenuManager({ restaurantId }) {
  const [items, setItems] = useState([]);
  const [formData, setFormData] = useState({ name: '', description: '', price: '', imageUrl: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchItems();
  }, [restaurantId]);

  async function fetchItems() {
    const response = await menuItemService.getForRestaurant(restaurantId);
    setItems(response.data);
  }

  function handleChange(e) {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  }

  async function handleAddItem(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      // price arrives from the input as a STRING ("12.50") — the backend's
      // BigDecimal/Jackson deserialization handles the numeric conversion,
      // but we still send it as a proper number here for a clean payload.
      await menuItemService.add(restaurantId, {
        ...formData,
        price: parseFloat(formData.price),
      });
      setFormData({ name: '', description: '', price: '', imageUrl: '' }); // reset the form
      fetchItems(); // refresh the list to show the new item immediately
    } catch (err) {
      setError(err.response?.data?.error || 'Could not add item.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="menu-manager">
      <h2>Your Menu</h2>

      {items.length === 0 ? (
        <p className="status-message">No items yet — add your first dish below.</p>
      ) : (
        <div className="owner-menu-list">
          {items.map((item) => (
            <div key={item.itemId} className="owner-menu-row">
              <span>{item.name}</span>
              <span>₹{item.price.toFixed(2)}</span>
              <span className={item.isAvailable ? 'available' : 'unavailable'}>
                {item.isAvailable ? 'Available' : 'Unavailable'}
              </span>
            </div>
          ))}
        </div>
      )}

      <form className="add-item-form" onSubmit={handleAddItem}>
        <h3>Add a new item</h3>
        {error && <p className="error-message">{error}</p>}

        <input
          type="text" name="name" placeholder="Item name"
          value={formData.name} onChange={handleChange} required
        />
        <input
          type="text" name="description" placeholder="Description (optional)"
          value={formData.description} onChange={handleChange}
        />
        <input
          type="number" name="price" placeholder="Price" step="0.01" min="0.01"
          value={formData.price} onChange={handleChange} required
        />
        <input
          type="url" name="imageUrl" placeholder="Image URL (optional)"
          value={formData.imageUrl} onChange={handleChange}
        />

        <button type="submit" disabled={submitting}>
          {submitting ? 'Adding...' : 'Add item'}
        </button>
      </form>
    </div>
  );
}

export default MenuManager;