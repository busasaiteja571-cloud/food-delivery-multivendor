import { useState, useEffect } from 'react';
import { adminService } from '../services/adminService.js';
import './AdminDashboardPage.css'
function AdminDashboardPage() {
  // Controls which panel is visible — a simple string, not a route,
  // since these three views don't need their own bookmarkable URLs
  // or browser back-button behavior for this internal tool.
  const [activeTab, setActiveTab] = useState('users');

  const [users, setUsers] = useState([]);
  const [restaurants, setRestaurants] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTabData(activeTab);
    // Re-runs whenever activeTab changes — switching tabs fetches that
    // tab's data fresh each time, rather than fetching everything
    // up-front on page load (which would waste requests on tabs the
    // admin might never open).
  }, [activeTab]);

  async function fetchTabData(tab) {
    setLoading(true);
    try {
      if (tab === 'users') {
        const res = await adminService.getUsers();
        setUsers(res.data);
      } else if (tab === 'restaurants') {
        const res = await adminService.getRestaurants();
        setRestaurants(res.data);
      } else if (tab === 'orders') {
        const res = await adminService.getOrders();
        setOrders(res.data);
      }
    } finally {
      setLoading(false);
    }
  }

  async function handleToggleStatus(restaurantId) {
    await adminService.toggleRestaurantStatus(restaurantId);
    fetchTabData('restaurants'); // refresh to show the new status immediately
  }

  return (
    <div className="admin-page">
      <h1>Admin Panel</h1>

      <div className="admin-tabs">
        {['users', 'restaurants', 'orders'].map((tab) => (
          <button
            key={tab}
            // Conditional className: applies "active" only to whichever
            // tab currently matches activeTab, giving the selected tab
            // distinct styling without any extra state.
            className={activeTab === tab ? 'tab-active' : ''}
            onClick={() => setActiveTab(tab)}
          >
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="status-message">Loading...</p>
      ) : (
        <>
          {activeTab === 'users' && (
            <table className="admin-table">
              <thead>
                <tr><th>Name</th><th>Email</th><th>Role</th></tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.userId}>
                    <td>{u.fullName}</td>
                    <td>{u.email}</td>
                    <td><span className="role-tag">{u.role}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {activeTab === 'restaurants' && (
            <table className="admin-table">
              <thead>
                <tr><th>Name</th><th>Address</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {restaurants.map((r) => (
                  <tr key={r.restaurantId}>
                    <td>{r.name}</td>
                    <td>{r.address}</td>
                    <td>
                      <span className={`status-badge ${r.isActive ? 'active' : 'inactive'}`}>
                        {r.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <button onClick={() => handleToggleStatus(r.restaurantId)}>
                        {r.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {activeTab === 'orders' && (
            <table className="admin-table">
              <thead>
                <tr><th>Restaurant</th><th>Status</th><th>Total</th><th>Placed</th></tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.orderId}>
                    <td>{o.restaurantName}</td>
                    <td><span className="role-tag">{o.status}</span></td>
                    <td>₹{o.totalAmount.toFixed(2)}</td>
                    <td>{new Date(o.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </div>
  );
}

export default AdminDashboardPage;