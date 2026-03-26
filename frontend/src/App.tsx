import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import VerifyEmailPage from './pages/VerifyEmailPage'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'

function MainLayout() {
  return (
    <>
      <Navbar />
      <Outlet />
    </>
  )
}

function App() {
  return (
    <Routes>
      {/* TODO: Sprint 6 — replace with conditional redirect (auth → dashboard, guest → landing page) */}
      <Route path="/" element={<Navigate to="/login" replace />} />

      {/* Strony auth — bez Navbar */}
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/verify" element={<VerifyEmailPage />} />

      {/* Trasy z Navbar (publiczne + chronione) */}
      <Route element={<MainLayout />}>
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<div>Dashboard (Sprint 2)</div>} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
