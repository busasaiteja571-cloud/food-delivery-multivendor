import { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import './DeliveryDashboardPage.css'
function DeliveryDashboardPage() {
  const [available, setAvailable] = useState([]);
  const [myDeliveries, setMyDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    fetchAll();
  }, []);

  // Fetches BOTH lists together, since claiming or delivering an order
  // always affects both at once (an order leaves "available" and enters
  // "mine", or leaves "mine" entirely once delivered).
  async function fetchAll() {
    setLoading(true);
    const [availableRes, mineRes] = await Promise.all([
      orderService.getAvailableDeliveries(),
      orderService.getMyDeliveries(),
    ]);
    setAvailable(availableRes.data);
    setMyDeliveries(mineRes.data);
    setLoading(false);
  }

  async function handleClaim(orderId) {
    setActionError('');
    try {
      await orderService.claim(orderId);
      fetchAll(); // refresh both lists — the claimed order moves lists
    } catch (err) {
      // Handles the exact race condition we discussed back in Phase 3:
      // if another agent claimed it a split second earlier, the backend
      // now correctly returns a 409, and we show that to the user
      // instead of pretending the claim succeeded.
      setActionError(err.response?.data?.error || 'Could not claim this order.');
      fetchAll(); // refresh anyway — the list is now stale either way
    }
  }

  async function handleDeliver(orderId) {
    setActionError('');
    try {
      await orderService.deliver(orderId);
      fetchAll();
    } catch (err) {
      setActionError(err.response?.data?.error || 'Could not update this order.');
    }
  }

  if (loading) return <p className="status-message">Loading deliveries...</p>;

  return (
    <div className="delivery-page">
      <h1>Delivery Dashboard</h1>

      {actionError && <p className="error-message">{actionError}</p>}

      <section>
        <h2>My active deliveries</h2>
        {myDeliveries.length === 0 ? (
          <p className="status-message">You have no active deliveries right now.</p>
        ) : (
          <div className="order-list">
            {myDeliveries.map((order) => (
              <div key={order.orderId} className="order-card">
                <div>
                  <h3>{order.restaurantName}</h3>
                  <p>{order.deliveryAddress}</p>
                  <p className="order-total">₹{order.totalAmount.toFixed(2)}</p>
                </div>
                <button onClick={() => handleDeliver(order.orderId)}>Mark Delivered</button>
              </div>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2>Available orders</h2>
        {available.length === 0 ? (
          <p className="status-message">No orders ready for pickup right now.</p>
        ) : (
          <div className="order-list">
            {available.map((order) => (
              <div key={order.orderId} className="order-card">
                <div>
                  <h3>{order.restaurantName}</h3>
                  <p>{order.deliveryAddress}</p>
                  <p className="order-total">₹{order.totalAmount.toFixed(2)}</p>
                </div>
                <button onClick={() => handleClaim(order.orderId)}>Claim</button>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default DeliveryDashboardPage;