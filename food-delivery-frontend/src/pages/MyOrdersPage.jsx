import { useState, useEffect } from 'react';
import { orderService } from '../services/orderService.js';
import './MyOrdersPage.css'
// A small lookup object mapping each status to a CSS class — cleaner
// than a chain of if/else statements inline in the JSX below.
const STATUS_CLASSES = {
  PLACED: 'status-placed',
  PREPARING: 'status-preparing',
  OUT_FOR_DELIVERY: 'status-delivering',
  DELIVERED: 'status-delivered',
  CANCELLED: 'status-cancelled',
};

function MyOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  async function fetchOrders() {
    setLoading(true);
    try {
      const response = await orderService.getMyOrders();
      setOrders(response.data);
    } finally {
      setLoading(false);
    }
  }

  if (loading) return <p className="status-message">Loading your orders...</p>;

  if (orders.length === 0) {
    return <p className="status-message">You haven't placed any orders yet.</p>;
  }

  return (
    <div className="orders-history-page">
      <h1>My Orders</h1>

      <div className="order-history-list">
        {orders.map((order) => (
          <div key={order.orderId} className="order-history-card">
            <div className="order-history-header">
              <h2>{order.restaurantName}</h2>
              <span className={`status-badge-lg ${STATUS_CLASSES[order.status]}`}>
                {order.status.replace(/_/g, ' ')}
              </span>
            </div>

            <p className="order-history-date">
              {new Date(order.createdAt).toLocaleString()}
            </p>

            <ul className="order-history-items">
              {order.items.map((item, idx) => (
                // Composite key: order items don't carry their own unique
                // ID in OrderItemResponse, so we combine index with name —
                // acceptable here ONLY because this list is never reordered
                // or filtered after render (a static, one-time display).
                <li key={`${order.orderId}-${idx}`}>
                  {item.quantity} × {item.menuItemName}
                  <span>₹{(item.priceAtOrder * item.quantity).toFixed(2)}</span>
                </li>
              ))}
            </ul>

            <div className="order-history-total">
              Total: ₹{order.totalAmount.toFixed(2)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default MyOrdersPage;