import { Routes, Route, Navigate, Outlet } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import VerifyEmailPage from './pages/VerifyEmailPage'
import HomePage from './pages/HomePage'
import CrewListPage from './pages/CrewListPage'
import ProtectedRoute from './components/ProtectedRoute'
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
        {/* Strony auth */}
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/verify" element={<VerifyEmailPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<div>Dashboard (Sprint 2)</div>} />
        </Route>
        {/* Strony informacyjne — Sprint 6 */}
        <Route path="/o-nas" element={<div>O nas (Sprint 6)</div>} />
        <Route path="/kontakt" element={<div>Kontakt (Sprint 6)</div>} />
        <Route path="/regulamin" element={<div>Regulamin (Sprint 6)</div>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
