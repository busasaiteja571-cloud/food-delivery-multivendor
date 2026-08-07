import { useState } from 'react';
import { useNotifications } from '../hooks/useNotifications.js';

function NotificationBell() {
  const { notifications, clearNotifications } = useNotifications();
  const [open, setOpen] = useState(false);

  function handleToggle() {
    setOpen(!open);
  }

  function handleClear() {
    clearNotifications();
    setOpen(false);
  }

  return (
    <div className="notification-bell-wrapper">
      <button className="notification-bell" onClick={handleToggle}>
        🔔
        {notifications.length > 0 && (
          <span className="notification-count">{notifications.length}</span>
        )}
      </button>

      {open && (
        <div className="notification-dropdown">
          {notifications.length === 0 ? (
            <p className="notification-empty">No new notifications</p>
          ) : (
            <>
              {notifications.map((n, idx) => (
                <div key={`${n.orderId}-${idx}`} className="notification-item">
                  {n.message}
                </div>
              ))}
              <button className="notification-clear" onClick={handleClear}>
                Clear all
              </button>
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default NotificationBell;