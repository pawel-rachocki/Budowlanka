import { Link } from 'react-router-dom'

const STEPS = [
  {
    icon: (
      <svg
        width="28"
        height="28"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <circle cx="11" cy="11" r="8" />
        <path d="m21 21-4.35-4.35" />
      </svg>
    ),
    title: 'Szukaj ekipy',
    description:
      'Przeglądaj profile ekip remontowych w Twoim mieście. Filtruj po specjalizacji i lokalizacji.',
  },
  {
    icon: (
      <svg
        width="28"
        height="28"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="M9 11l3 3L22 4" />
        <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
      </svg>
    ),
    title: 'Porównaj oferty',
    description:
      'Sprawdź portfolio, opinie klientów i zakres usług. Wybierz najlepszą ekipę dla projektu.',
  },
  {
    icon: (
      <svg
        width="28"
        height="28"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <polyline points="16 11 18 13 22 9" />
      </svg>
    ),
    title: 'Zatrudnij fachowców',
    description:
      'Skontaktuj się bezpośrednio z wybraną ekipą. Bez pośredników, bez ukrytych kosztów.',
  },
]

export default function HomePage() {
  return (
    <>
      {/* Hero */}
      <section className="relative overflow-hidden">
        {/* Blueprint grid */}
        <div
          className="absolute inset-0 pointer-events-none"
          aria-hidden="true"
          style={{
            backgroundImage: `
              repeating-linear-gradient(0deg, transparent, transparent 59px, rgba(45,90,142,0.04) 59px, rgba(45,90,142,0.04) 60px),
              repeating-linear-gradient(90deg, transparent, transparent 59px, rgba(45,90,142,0.04) 59px, rgba(45,90,142,0.04) 60px)
            `,
          }}
        />
        {/* Warm glow */}
        <div
          className="absolute -top-32 -right-32 w-[28rem] h-[28rem] rounded-full bg-brand-500/[0.05] blur-3xl pointer-events-none"
          aria-hidden="true"
        />

        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20 sm:py-28 lg:py-36">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold text-brand-500 tracking-wide uppercase mb-4">
              Marketplace ekip remontowych
            </p>

            <h1 className="text-4xl sm:text-5xl lg:text-[3.5rem] font-black tracking-tight text-navy-900 leading-[1.08]">
              Znajdź sprawdzoną{' '}
              <span className="relative inline-block">
                <span className="relative z-10 text-brand-500">ekipę remontową</span>
                <span
                  className="absolute bottom-1 left-0 w-full h-3 bg-brand-500/10 rounded"
                  aria-hidden="true"
                />
              </span>{' '}
              w&nbsp;swoim mieście
            </h1>

            <p className="mt-6 text-lg sm:text-xl text-navy-600 leading-relaxed max-w-xl">
              Portal łączący ekipy remontowe z&nbsp;klientami w&nbsp;całej Polsce. Sprawdzone
              profile, opinie i&nbsp;portfolio&nbsp;&mdash; wszystko w&nbsp;jednym miejscu.
            </p>

            <div className="mt-10 flex flex-col sm:flex-row gap-4">
              <Link
                to="/ekipy"
                className="inline-flex items-center justify-center gap-2 px-7 py-3.5 text-base font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-lg shadow-sm shadow-brand-500/20 transition-all hover:shadow-md hover:shadow-brand-500/25 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
              >
                Znajdź ekipę
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <path d="M5 12h14M12 5l7 7-7 7" />
                </svg>
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center justify-center px-7 py-3.5 text-base font-semibold text-navy-700 border border-navy-200 hover:border-navy-300 bg-surface-card hover:bg-white rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
              >
                Zarejestruj się
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="bg-surface-card py-16 sm:py-20 lg:py-24 border-t border-navy-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h2 className="text-2xl sm:text-3xl font-bold text-navy-900">Jak to działa?</h2>
            <p className="mt-3 text-navy-600 max-w-md mx-auto">
              Trzy proste kroki dzielą Cię od wymarzonego remontu
            </p>
          </div>

          <div className="mt-14 grid grid-cols-1 sm:grid-cols-3 gap-8 lg:gap-12">
            {STEPS.map((step, i) => (
              <div key={step.title} className="relative text-center">
                <span
                  className="block text-5xl font-black text-navy-900/[0.06] mb-3 select-none"
                  aria-hidden="true"
                >
                  0{i + 1}
                </span>
                <div className="inline-flex w-14 h-14 rounded-xl bg-brand-50 text-brand-500 items-center justify-center mb-4">
                  {step.icon}
                </div>
                <h3 className="text-lg font-bold text-navy-900">{step.title}</h3>
                <p className="mt-2 text-sm text-navy-600 leading-relaxed max-w-xs mx-auto">
                  {step.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Bottom CTA */}
      <section className="py-16 sm:py-20 border-t border-navy-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-2xl sm:text-3xl font-bold text-navy-900">Jesteś ekipą remontową?</h2>
          <p className="mt-3 text-navy-600 max-w-md mx-auto">
            Dołącz do portalu i&nbsp;docieraj do nowych klientów w&nbsp;swojej okolicy.
          </p>
          <Link
            to="/register"
            className="inline-flex items-center justify-center mt-8 px-7 py-3.5 text-base font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-lg shadow-sm shadow-brand-500/20 transition-all hover:shadow-md hover:shadow-brand-500/25 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
          >
            Wystaw swój profil
          </Link>
        </div>
      </section>
    </>
  )
}
