import { Link } from 'react-router-dom'
import WrenchIcon from './icons/WrenchIcon'

const CURRENT_YEAR = new Date().getFullYear()

const FOOTER_LINKS = [
  { to: '/o-nas', label: 'O nas' },
  { to: '/kontakt', label: 'Kontakt' },
  { to: '/regulamin', label: 'Regulamin' },
]

export default function Footer() {
  return (
    <footer className="bg-surface-card border-t border-navy-100">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <span className="flex items-center justify-center w-6 h-6 rounded-md bg-brand-500 text-white shrink-0">
              <WrenchIcon size={14} />
            </span>
            <span className="flex flex-col leading-none">
              <span className="text-[11px] font-semibold tracking-wide text-navy-600 uppercase">
                Ekipy
              </span>
              <span className="text-[13px] font-black tracking-tight text-navy-900">Remontowe</span>
            </span>
          </div>

          {/* Links */}
          <nav aria-label="Linki stopki">
            <ul className="flex items-center gap-6">
              {FOOTER_LINKS.map(({ to, label }) => (
                <li key={to}>
                  <Link
                    to={to}
                    className="text-sm text-navy-600 hover:text-brand-500 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 rounded"
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        {/* Copyright */}
        <p className="mt-4 text-xs text-muted sm:text-center">
          © {CURRENT_YEAR} Portal Ekipy Remontowe. Wszelkie prawa zastrzeżone.
        </p>
      </div>
    </footer>
  )
}
