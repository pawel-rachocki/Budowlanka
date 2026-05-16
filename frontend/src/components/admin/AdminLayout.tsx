import { NavLink, Outlet } from 'react-router-dom'

const NAV_LINKS = [
  { to: '/admin/moderation', label: 'Moderacja zdjęć', Icon: PhotoIcon },
  { to: '/admin/crews', label: 'Ekipy', Icon: UsersIcon },
] as const

export default function AdminLayout() {
  return (
    <div className="flex min-h-full flex-1">
      {/* Sidebar — desktop */}
      <aside className="sticky top-0 hidden h-screen w-64 shrink-0 flex-col bg-navy-900 lg:flex">
        <div className="flex items-center gap-3 border-b border-white/10 px-6 py-5">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-brand-500 text-white">
            <ShieldIcon />
          </div>
          <span className="text-sm font-semibold leading-tight text-white">
            Panel administratora
          </span>
        </div>

        <nav className="flex-1 space-y-0.5 px-3 py-4" aria-label="Menu administratora">
          {NAV_LINKS.map(({ to, label, Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                [
                  'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-white/10 text-white'
                    : 'text-navy-200 hover:bg-white/5 hover:text-white',
                ].join(' ')
              }
            >
              <Icon />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      {/* Główna treść */}
      <div className="flex min-w-0 flex-1 flex-col bg-surface pb-16 lg:pb-0">
        <Outlet />
      </div>

      {/* Tab bar — mobile */}
      <nav
        className="fixed bottom-0 left-0 right-0 z-50 flex border-t border-white/10 bg-navy-900 lg:hidden"
        aria-label="Skróty administratora"
      >
        {NAV_LINKS.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              [
                'flex flex-1 flex-col items-center gap-1 px-2 py-3 text-xs font-medium transition-colors',
                isActive
                  ? 'bg-white/10 text-white'
                  : 'text-navy-200 hover:bg-white/5 hover:text-white',
              ].join(' ')
            }
          >
            <Icon />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}

function ShieldIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  )
}

function PhotoIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="9" cy="9" r="2" />
      <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" />
    </svg>
  )
}

function UsersIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}
