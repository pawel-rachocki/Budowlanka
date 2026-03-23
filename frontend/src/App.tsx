import { Routes, Route, Navigate } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'

function App() {
  return (
    <Routes>
      {/* TODO: Sprint 6 — replace with conditional redirect (auth → dashboard, guest → landing page) */}
      <Route path="/" element={<Navigate to="/register" replace />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/login"
        element={<div className="p-8 text-center text-gray-500">Login — coming soon</div>}
      />
    </Routes>
  )
}

export default App
