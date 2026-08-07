import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* AuthProvider wraps App, meaning EVERY component in the entire
        app tree can now call useAuth() and get real, live data. */}
    <AuthProvider>
      <App />
    </AuthProvider>
    
  </StrictMode>,
)
