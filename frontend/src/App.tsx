import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import VerifyEmailPage from './pages/VerifyEmailPage'
import HomePage from './pages/HomePage'
import CrewListPage from './pages/CrewListPage'
import CrewProfilePage from './pages/CrewProfilePage'
import CrewDashboardPage from './pages/CrewDashboardPage'
import AdminModerationPage from './pages/admin/AdminModerationPage'
import AdminCrewListPage from './pages/admin/AdminCrewListPage'
import ProtectedRoute from './components/ProtectedRoute'
import AdminProtectedRoute from './components/admin/AdminProtectedRoute'
import AdminLayout from './components/admin/AdminLayout'
import Navbar from './components/Navbar'
import Footer from './components/Footer'

function MainLayout() {
  return (
    <div className="flex flex-col min-h-screen">
      <Navbar />
      <main className="flex-1 flex flex-col">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}

function App() {
  return (
    <Routes>
      {/* Wszystkie strony z Layout (Navbar + Footer) */}
      <Route element={<MainLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/ekipy" element={<CrewListPage />} />
        <Route path="/ekipy/:slug" element={<CrewProfilePage />} />
        {/* Strony auth */}
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/verify" element={<VerifyEmailPage />} />
        <Route element={<ProtectedRoute requiredRole="CREW" />}>
          <Route path="/dashboard" element={<CrewDashboardPage />} />
        </Route>
        {/* Strony informacyjne — Sprint 6 */}
        <Route path="/o-nas" element={<div>O nas (Sprint 6)</div>} />
        <Route path="/kontakt" element={<div>Kontakt (Sprint 6)</div>} />
        <Route path="/regulamin" element={<div>Regulamin (Sprint 6)</div>} />
      </Route>

      {/* Panel admina — własny layout (bez Navbar/Footer) */}
      <Route element={<AdminProtectedRoute />}>
        <Route path="/admin" element={<Navigate to="/admin/moderation" replace />} />
        <Route element={<AdminLayout />}>
          <Route path="/admin/moderation" element={<AdminModerationPage />} />
          <Route path="/admin/crews" element={<AdminCrewListPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
