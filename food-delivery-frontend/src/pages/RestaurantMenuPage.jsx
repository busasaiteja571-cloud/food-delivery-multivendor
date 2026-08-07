import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { menuItemService } from '../services/menuItemService.js';

function RestaurantMenuPage() {
  // useParams() reads dynamic segments straight out of the URL, matching
  // whatever we named them in the <Route path="/restaurants/:restaurantId">
  // definition. Whatever's actually in the browser's address bar — "5",
  // "12", etc. — lands here as a string.
  const { restaurantId } = useParams();
  const navigate = useNavigate();

  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // cart is a plain object keyed by menuItemId, e.g. { 3: 2, 7: 1 } means
  // "2 of item #3, 1 of item #7." An object (not an array) makes
  // "increment this specific item's quantity" a simple, direct lookup
  // rather than searching through an array every time.
  const [cart, setCart] = useState({});

  const [showAvailableOnly, setShowAvailableOnly] = useState(false);

  useEffect(() => {
    async function fetchMenu() {
      try {
        const response = await menuItemService.getForRestaurant(restaurantId);
        setMenuItems(response.data);
      } catch (err) {
        setError('Could not load this menu. Please try again later.');
      } finally {
        setLoading(false);
      }
    }

    fetchMenu();
    // restaurantId is listed as a dependency: if the user navigates from
    // one restaurant's menu directly to another (URL changes, but this
    // component doesn't fully unmount/remount), this effect re-runs and
    // fetches the NEW restaurant's menu instead of showing stale data.
  }, [restaurantId]);

  function addToCart(itemId) {
    setCart((prevCart) => ({
      ...prevCart,
      // Falls back to 0 if this item isn't in the cart yet, then adds 1.
      [itemId]: (prevCart[itemId] || 0) + 1,
    }));
  }

  function removeFromCart(itemId) {
    setCart((prevCart) => {
      const currentQty = prevCart[itemId] || 0;
      if (currentQty <= 1) {
        // Destructure out the item being removed, keep everything else —
        // this is how you cleanly delete one key from a state object
        // without mutating it directly.
        const { [itemId]: _, ...rest } = prevCart;
        return rest;
      }
      return { ...prevCart, [itemId]: currentQty - 1 };
    });
  }

  // Derived value, NOT its own useState — it's always fully computable
  // from menuItems + cart, so storing it separately would risk the two
  // falling out of sync. Recalculating on every render is cheap here.
  const cartTotal = menuItems.reduce((sum, item) => {
    const qty = cart[item.itemId] || 0;
    return sum + item.price * qty;
  }, 0);

  const cartItemCount = Object.values(cart).reduce((sum, qty) => sum + qty, 0);

  // Another derived value — a pure filter over data we already have in
  // memory. No network request, no extra fetch: just a different VIEW
  // of the same menuItems array, computed fresh on every render.
  const visibleItems = showAvailableOnly
    ? menuItems.filter((item) => item.isAvailable)
    : menuItems;

  function goToCheckout() {
    navigate('/checkout', {
      // The `state` option attaches data to this specific navigation —
      // it's not stored in the URL or localStorage, just passed along
      // for the destination page to read via useLocation().
      state: { restaurantId, cart, menuItems },
    });
  }

  if (loading) return <p className="status-message">Loading menu...</p>;
  if (error) return <p className="status-message error-message">{error}</p>;

  return (
    <div className="menu-page">
      <h1>Menu</h1>

      <label className="filter-toggle">
        <input
          type="checkbox"
          checked={showAvailableOnly}
          onChange={(e) => setShowAvailableOnly(e.target.checked)}
        />
        Show available items only
      </label>

      {visibleItems.length === 0 ? (
        <p className="status-message">
          {menuItems.length === 0
            ? "This restaurant hasn't added any items yet."
            : 'No items match this filter.'}
        </p>
      ) : (
        <div className="menu-list">
          {visibleItems.map((item) => (
            <div key={item.itemId} className="menu-item-row">
              <div className="menu-item-info">
                <h3>{item.name}</h3>
                {item.description && <p>{item.description}</p>}
                <p className="menu-item-price">₹{item.price.toFixed(2)}</p>
              </div>

              {!item.isAvailable ? (
                <span className="unavailable-tag">Unavailable</span>
              ) : cart[item.itemId] ? (
                <div className="quantity-control">
                  <button onClick={() => removeFromCart(item.itemId)}>−</button>
                  <span>{cart[item.itemId]}</span>
                  <button onClick={() => addToCart(item.itemId)}>+</button>
                </div>
              ) : (
                <button className="add-btn" onClick={() => addToCart(item.itemId)}>
                  Add
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {cartItemCount > 0 && (
        <div className="cart-summary-bar">
          <span>{cartItemCount} item{cartItemCount > 1 ? 's' : ''} · ₹{cartTotal.toFixed(2)}</span>
          <button className="checkout-btn" onClick={goToCheckout}>Go to checkout</button>
        </div>
      )}
    </div>
  );
}

export default RestaurantMenuPage;