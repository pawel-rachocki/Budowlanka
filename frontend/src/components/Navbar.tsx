import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import WrenchIcon from './icons/WrenchIcon'

const MenuIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <line x1="3" y1="6" x2="21" y2="6" />
    <line x1="3" y1="12" x2="21" y2="12" />
    <line x1="3" y1="18" x2="21" y2="18" />
  </svg>
)

const CloseIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
  >
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
)

function truncateEmail(email: string, maxLen = 24): string {
  if (email.length <= maxLen) return email
  const atIndex = email.lastIndexOf('@')
  if (atIndex === -1) return email.slice(0, maxLen) + '…'
  const domain = email.slice(atIndex)
  const localMaxLen = Math.max(1, maxLen - domain.length - 1)
  return email.slice(0, localMaxLen) + '…' + domain
}

function getInitial(email: string): string {
  return email.charAt(0).toUpperCase()
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const navRef = useRef<HTMLElement>(null)

  useEffect(() => {
    if (!menuOpen) return
    function handleClickOutside(e: MouseEvent) {
      if (navRef.current && !navRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [menuOpen])

  async function handleLogout() {
    setMenuOpen(false)
    try {
      await logout()
    } finally {
      navigate('/login')
    }
  }

  return (
    <nav
      ref={navRef}
      className="sticky top-0 z-50 bg-surface-card border-b border-navy-100 shadow-sm"
    >
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center gap-2.5 group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 rounded-lg"
          >
            <span className="flex items-center justify-center w-8 h-8 rounded-lg bg-brand-500 text-white shrink-0 group-hover:bg-brand-600 transition-colors">
              <WrenchIcon />
            </span>
            <span className="flex flex-col leading-none">
              <span className="text-[13px] font-semibold tracking-wide text-navy-600 uppercase">
                Ekipy
              </span>
              <span className="text-[15px] font-black tracking-tight text-navy-900 group-hover:text-brand-500 transition-colors">
                Remontowe
              </span>
            </span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden sm:flex items-center gap-2">
            <NavLink
              to="/ekipy"
              className={({ isActive }) =>
                `px-4 py-2 text-sm font-medium transition-colors rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 ${
                  isActive ? 'text-brand-500 font-semibold' : 'text-navy-700 hover:text-navy-900'
                }`
              }
            >
              Ekipy
            </NavLink>
            <span className="w-px h-5 bg-navy-100" aria-hidden="true" />
            {user ? (
              <>
                {user.role === 'CREW' && (
                  <NavLink
                    to="/dashboard"
                    className={({ isActive }) =>
                      `px-4 py-2 text-sm font-medium transition-colors rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 ${
                        isActive ? 'text-brand-500 font-semibold' : 'text-navy-700 hover:text-navy-900'
                      }`
                    }
                  >
                    Mój profil
                  </NavLink>
                )}
                {user.role === 'ADMIN' && (
                  <NavLink
                    to="/admin/moderation"
                    className={({ isActive }) =>
                      `px-4 py-2 text-sm font-medium transition-colors rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 ${
                        isActive ? 'text-brand-500 font-semibold' : 'text-navy-700 hover:text-navy-900'
                      }`
                    }
                  >
                    Panel admina
                  </NavLink>
                )}
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface border border-navy-100">
                  <div className="w-6 h-6 rounded-full bg-brand-500 flex items-center justify-center text-white text-[11px] font-bold shrink-0">
                    {getInitial(user.email)}
                  </div>
                  <span className="text-sm text-navy-700 max-w-45 truncate">
                    {truncateEmail(user.email)}
                  </span>
                </div>
                <button
                  onClick={handleLogout}
                  className="px-4 py-2 text-sm font-medium text-navy-700 border border-navy-100 rounded-lg hover:bg-surface hover:border-navy-200 hover:text-navy-900 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
                >
                  Wyloguj
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-navy-700 hover:text-navy-900 transition-colors rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
                >
                  Zaloguj się
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
                >
                  Zarejestruj się
                </Link>
              </>
            )}
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="sm:hidden p-2 rounded-lg text-navy-600 hover:bg-surface hover:text-navy-900 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
            aria-label={menuOpen ? 'Zamknij menu' : 'Otwórz menu'}
            aria-expanded={menuOpen}
            aria-controls="mobile-menu"
          >
            {menuOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>

        {/* Mobile menu */}
        {menuOpen && (
          <div
            id="mobile-menu"
            className="sm:hidden border-t border-navy-100 py-3 flex flex-col gap-1"
          >
            <NavLink
              to="/ekipy"
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `block px-3 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                  isActive ? 'text-brand-500 font-semibold bg-surface' : 'text-navy-700 hover:bg-surface'
                }`
              }
            >
              Ekipy
            </NavLink>
            {user ? (
              <>
                <div className="flex items-center gap-2.5 px-3 py-2 rounded-lg bg-surface mb-1">
                  <div className="w-7 h-7 rounded-full bg-brand-500 flex items-center justify-center text-white text-xs font-bold shrink-0">
                    {getInitial(user.email)}
                  </div>
                  <span className="text-sm text-navy-700 truncate">{user.email}</span>
                </div>
                {user.role === 'CREW' && (
                  <NavLink
                    to="/dashboard"
                    onClick={() => setMenuOpen(false)}
                    className={({ isActive }) =>
                      `block px-3 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                        isActive ? 'text-brand-500 font-semibold bg-surface' : 'text-navy-700 hover:bg-surface'
                      }`
                    }
                  >
                    Mój profil
                  </NavLink>
                )}
                {user.role === 'ADMIN' && (
                  <NavLink
                    to="/admin/moderation"
                    onClick={() => setMenuOpen(false)}
                    className={({ isActive }) =>
                      `block px-3 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                        isActive ? 'text-brand-500 font-semibold bg-surface' : 'text-navy-700 hover:bg-surface'
                      }`
                    }
                  >
                    Panel admina
                  </NavLink>
                )}
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-3 py-2.5 text-sm font-medium text-navy-700 hover:bg-surface rounded-lg transition-colors"
                >
                  Wyloguj
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  onClick={() => setMenuOpen(false)}
                  className="block px-3 py-2.5 text-sm font-medium text-navy-700 hover:bg-surface rounded-lg transition-colors"
                >
                  Zaloguj się
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMenuOpen(false)}
                  className="block px-3 py-2.5 text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-lg transition-colors text-center"
                >
                  Zarejestruj się
                </Link>
              </>
            )}
          </div>
        )}
      </div>
    </nav>
  )
}
