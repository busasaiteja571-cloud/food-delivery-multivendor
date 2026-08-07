import { useState, useEffect, useRef } from 'react';
import { notificationService } from '../services/notificationService.js';
import { useAuth } from '../context/AuthContext';

const POLL_INTERVAL_MS = 15000; // check every 15 seconds

// A custom hook: a reusable function starting with "use" that itself
// uses other hooks internally. This lets ANY component just call
// useNotifications() and get live data, without duplicating the
// polling/fetching logic in every place that needs it.
export function useNotifications() {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);

  // useRef, not useState: we need to remember the "since" timestamp
  // across renders, but changing it should NOT trigger a re-render by
  // itself — it's bookkeeping for the fetch logic, not something the
  // UI directly displays.
  const lastCheckedRef = useRef(new Date().toISOString());

  useEffect(() => {
    if (!isAuthenticated) return;

    async function poll() {
      try {
        const response = await notificationService.getNotifications(lastCheckedRef.current);
        if (response.data.length > 0) {
          setNotifications((prev) => [...response.data, ...prev]);
        }
        lastCheckedRef.current = new Date().toISOString();
      } catch {
        // Silently skip a failed poll — a temporary network hiccup
        // shouldn't surface as an error to the user; we'll just try
        // again on the next interval.
      }
    }

    // setInterval schedules `poll` to run repeatedly, every
    // POLL_INTERVAL_MS milliseconds, until we explicitly stop it.
    const intervalId = setInterval(poll, POLL_INTERVAL_MS);

    // The CLEANUP function: React calls this automatically when the
    // component using this hook unmounts (e.g. user navigates away or
    // logs out). Without this, the interval would keep running forever
    // in the background, silently leaking memory and making requests
    // for a component that no longer exists.
    return () => clearInterval(intervalId);
  }, [isAuthenticated]);

  function clearNotifications() {
    setNotifications([]);
  }

  return { notifications, clearNotifications };
}