import { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';

function RestaurantOrders({ restaurantId }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, [restaurantId]);

  async function fetchOrders() {
    setLoading(true);
    const response = await orderService.getForRestaurant(restaurantId);
    setOrders(response.data);
    setLoading(false);
  }

  async function handleMarkPreparing(orderId) {
    await orderService.markPreparing(restaurantId, orderId);
    fetchOrders();
  }

  if (loading) return <p className="status-message">Loading orders...</p>;

  return (
    <div className="restaurant-orders">
      <h2>Incoming Orders</h2>
      {orders.length === 0 ? (
        <p className="status-message">No orders yet.</p>
      ) : (
        <div className="order-list">
          {orders.map((order) => (
            <div key={order.orderId} className="order-card">
              <div>
                <h3>Order #{order.orderId} — {order.status}</h3>
                <p>{order.deliveryAddress}</p>
                <p className="order-total">₹{order.totalAmount.toFixed(2)}</p>
              </div>
              {order.status === 'PLACED' && (
                <button onClick={() => handleMarkPreparing(order.orderId)}>
                  Start Preparing
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RestaurantOrders;