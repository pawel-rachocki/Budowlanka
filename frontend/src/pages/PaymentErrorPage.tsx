import { Link } from 'react-router-dom'

// Strona powrotu dla przerwanej/anulowanej płatności. Realny status transakcji pochodzi
// z webhooka P24 (nie z return URL), więc ta strona jest wyłącznie informacyjna i kieruje
// ekipę do ponownej próby zakupu pakietu.
export default function PaymentErrorPage() {
  return (
    <div className="min-h-full flex-1 bg-surface">
      <div className="mx-auto flex max-w-lg flex-col items-center px-4 py-16 text-center sm:px-6 lg:py-24">
        <span
          className="flex h-16 w-16 items-center justify-center rounded-full bg-red-100 text-red-600"
          aria-hidden
        >
          <AlertTriangleIcon />
        </span>
        <h1 className="mt-6 text-2xl font-bold tracking-tight text-navy-900 sm:text-3xl">
          Płatność nie została zakończona
        </h1>
        <p className="mt-3 text-base text-navy-600">
          Transakcja została anulowana lub przerwana i nie pobraliśmy żadnych środków. Możesz
          spróbować ponownie — Twój pakiet nie został aktywowany.
        </p>

        <div className="mt-8 flex flex-col gap-3 sm:flex-row">
          <Link
            to="/ekipa/pakiety"
            className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
          >
            Spróbuj ponownie
          </Link>
          <Link
            to="/dashboard"
            className="inline-flex items-center justify-center rounded-lg border border-navy-200 px-5 py-2.5 text-sm font-semibold text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
          >
            Wróć do panelu
          </Link>
        </div>
      </div>
    </div>
  )
}

function AlertTriangleIcon() {
  return (
    <svg
      width="32"
      height="32"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  )
}
