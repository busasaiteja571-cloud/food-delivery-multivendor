import { createContext, useContext, useState } from 'react';

// createContext() makes a "channel" that any descendant component can
// tune into. The argument passed here is just the DEFAULT value used
// only if a component tries to read it with no <AuthProvider> above it
// in the tree — a safety fallback, not the real data source.
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Lazy initializer function: this function only runs ONCE, on the
  // very first render, to read localStorage. Without wrapping it in a
  // function (i.e. writing useState(JSON.parse(...)) directly), that
  // parse would re-run on every single render for no reason.
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  const [token, setToken] = useState(() => localStorage.getItem('token'));

  // Called once, right after a successful /auth/login or /auth/register
  // response — this is now the ONLY place that writes to localStorage
  // AND updates React state, keeping both in sync automatically.
  function login(authResponse) {
    const { token, ...userData } = authResponse;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(token);
    setUser(userData);
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }

  // Whatever we put in `value` is what every descendant component
  // receives when it calls useAuth(). isAuthenticated is a convenience
  // so components don't all have to independently write `!!token`.
  const value = {
    user,
    token,
    isAuthenticated: !!token,
    login,
    logout,
  };

  // .Provider is what actually "broadcasts" the value down to every
  // descendant wrapped inside it — that's `children`, i.e. the rest
  // of your entire app.
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// A small custom hook wrapping useContext(AuthContext) — this means
// components write `const { user, logout } = useAuth();` instead of
// `const { user, logout } = useContext(AuthContext);` everywhere,
// which is both shorter and lets us add safety checks in one place.
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    // This fires only if someone calls useAuth() OUTSIDE of
    // <AuthProvider> entirely — a real bug worth catching loudly
    // rather than silently returning broken/undefined data.
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}