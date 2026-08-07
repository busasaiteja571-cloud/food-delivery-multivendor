import { useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { orderService } from '../services/orderService.js';
import './CheckoutPage.css'
function CheckoutPage() {
  // useLocation() gives access to the `state` object that was attached
  // during navigate('/checkout', { state: {...} }) on the previous page.
  const location = useLocation();
  const navigate = useNavigate();

  const { restaurantId, cart, menuItems } = location.state || {};

  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmedOrder, setConfirmedOrder] = useState(null);

  // Defensive guard: if someone lands on /checkout directly (typed URL,
  // refreshed the page, bookmarked it) with no cart data attached, there's
  // nothing to check out — send them back to browse restaurants instead
  // of showing a broken, empty checkout screen.
  if (!cart || Object.keys(cart).length === 0) {
    return <Navigate to="/" replace />;
  }

  // Build the exact line-item list, keeping this derived from cart +
  // menuItems (both already fetched) rather than re-fetching anything.
  const cartLines = menuItems
    .filter((item) => cart[item.itemId])
    .map((item) => ({
      itemId: item.itemId,
      name: item.name,
      price: item.price,
      quantity: cart[item.itemId],
    }));

  const total = cartLines.reduce((sum, line) => sum + line.price * line.quantity, 0);

  async function handlePlaceOrder(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    // The request body matches PlaceOrderRequest EXACTLY: deliveryAddress
    // and a list of { menuItemId, quantity }. No price field exists here
    // at all — the backend independently looks up and trusts only its
    // own database's current prices, per the Phase 3 design.
    const requestBody = {
      deliveryAddress,
      items: cartLines.map((line) => ({
        menuItemId: line.itemId,
        quantity: line.quantity,
      })),
    };

    try {
      const response = await orderService.place(restaurantId, requestBody);
      setConfirmedOrder(response.data);
    } catch (err) {
      const message = err.response?.data?.error || 'Could not place order. Please try again.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  // After a successful order, replace the whole checkout form with a
  // confirmation view — no reason to show an editable form for an
  // order that's already been placed.
  if (confirmedOrder) {
    return (
      <div className="checkout-page">
        <div className="order-confirmation">
          <h1>Order placed!</h1>
          <p>Your order from <strong>{confirmedOrder.restaurantName}</strong> is being prepared.</p>
          <p className="confirmation-total">Total: ₹{confirmedOrder.totalAmount.toFixed(2)}</p>
          <button onClick={() => navigate('/')}>Back to restaurants</button>
        </div>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <h1>Checkout</h1>

      <div className="order-summary">
        {cartLines.map((line) => (
          <div key={line.itemId} className="summary-line">
            <span>{line.quantity} × {line.name}</span>
            <span>₹{(line.price * line.quantity).toFixed(2)}</span>
          </div>
        ))}
        <div className="summary-line summary-total">
          <span>Total</span>
          <span>₹{total.toFixed(2)}</span>
        </div>
      </div>

      <form className="checkout-form" onSubmit={handlePlaceOrder}>
        {error && <p className="error-message">{error}</p>}

        <label>
          Delivery Address
          <textarea
            value={deliveryAddress}
            onChange={(e) => setDeliveryAddress(e.target.value)}
            required
            rows={3}
            placeholder="Flat / street / landmark / city"
          />
        </label>

        <button type="submit" disabled={submitting}>
          {submitting ? 'Placing order...' : `Place order · ₹${total.toFixed(2)}`}
        </button>
      </form>
    </div>
  );
}

export default CheckoutPage;