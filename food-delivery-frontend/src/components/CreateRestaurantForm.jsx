import { useState } from 'react';
import { restaurantService } from '../services/restaurantService.js';
import './CreateRestaurantForm.css'
// onCreated: a function PROP, not a hook — this is how a child component
// notifies its parent that something happened, since state naturally
// flows down (props) but events need to flow back up (callbacks).
function CreateRestaurantForm({ onCreated }) {
  const [formData, setFormData] = useState({ name: '', description: '', address: '', imageUrl: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      await restaurantService.create(formData);
      onCreated(); // tell the parent dashboard to re-fetch and switch views
    } catch (err) {
      setError(err.response?.data?.error || 'Could not create restaurant.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="create-restaurant-form" onSubmit={handleSubmit}>
      <h2>Set up your restaurant</h2>
      {error && <p className="error-message">{error}</p>}

      <label>
        Restaurant Name
        <input type="text" name="name" value={formData.name} onChange={handleChange} required />
      </label>

      <label>
        Description
        <textarea name="description" value={formData.description} onChange={handleChange} rows={3} />
      </label>

      <label>
        Address
        <input type="text" name="address" value={formData.address} onChange={handleChange} required />
      </label>

      <label>
        Image URL
        <input
          type="url"
          name="imageUrl"
          placeholder="https://example.com/photo.jpg"
          value={formData.imageUrl}
          onChange={handleChange}
        />
      </label>
      
      {formData.imageUrl && (
        <img 
          src={formData.imageUrl} 
          alt="Preview" 
          className="image-preview"
          onError={(e) => { e.target.style.display = 'none'; }} 
        />
      )}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Creating...' : 'Create restaurant'}
      </button>
    </form>
  );
}

export default CreateRestaurantForm;