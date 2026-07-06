import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useBoostPackages } from '../hooks/usePackages'
import { useInitiateBoostPayment } from '../hooks/usePayments'
import { useMySubscription } from '../hooks/useMySubscription'
import type { BoostPackage } from '../types/package.types'
import { formatPrice } from '../utils/formatPrice'

// Boost 30-dniowy jest wyróżniany jako „polecany" (najlepszy stosunek ceny do czasu).
const RECOMMENDED_DURATION_DAYS = 30

export default function BoostSelectionPage() {
  const { packages, isLoading, error } = useBoostPackages()
  const { initiateBoostPayment, isPending } = useInitiateBoostPayment()

  // Status subskrypcji steruje banerem soft-gate — Boost podbija tylko widoczny profil.
  // Backend nie blokuje zakupu bez subskrypcji, więc to wyłącznie ostrzeżenie, nie blokada.
  const { subscription } = useMySubscription()
  const noActiveSubscription = subscription?.hasActiveSubscription === false

  // Który pakiet aktualnie inicjuje płatność — isPending z mutacji jest globalny dla hooka,
  // więc lokalnie śledzimy klikniętą kartę, by pokazać spinner tylko na niej.
  const [selectedId, setSelectedId] = useState<string | null>(null)

  async function handleSelect(pkg: BoostPackage) {
    setSelectedId(pkg.id)
    try {
      // Sukces przekierowuje do bramki P24 (obsługa w hooku); błąd emituje toast w hooku.
      await initiateBoostPayment(pkg.id)
    } catch {
      // Redirect się nie wykonał — odblokuj karty, by ekipa mogła spróbować ponownie.
      setSelectedId(null)
    }
  }

  return (
    <div className="min-h-full flex-1 bg-surface">
      <div className="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:px-8">
        {/* Nagłówek */}
        <header className="mx-auto mb-10 max-w-2xl text-center">
          <span className="mb-3 inline-block rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-brand-700">
            Boost
          </span>
          <h1 className="text-3xl font-bold tracking-tight text-navy-900 sm:text-4xl">
            Wyróżnij się i zdobądź więcej zleceń
          </h1>
          <p className="mt-3 text-base text-navy-600">
            Boost podnosi Twój profil na wyższe pozycje w wynikach wyszukiwania ekip przez wybrany
            okres. Więcej wyświetleń to więcej zapytań od klientów. Płatność obsługuje Przelewy24.
          </p>
        </header>

        {noActiveSubscription && <NoSubscriptionBanner />}

        {isLoading && <LoadingState />}
        {!isLoading && error && <ErrorState />}
        {!isLoading && !error && packages.length === 0 && <EmptyState />}

        {!isLoading && !error && packages.length > 0 && (
          <div className="mx-auto grid max-w-2xl grid-cols-1 gap-6 sm:grid-cols-2">
            {packages.map((pkg) => (
              <BoostCard
                key={pkg.id}
                pkg={pkg}
                recommended={pkg.durationDays === RECOMMENDED_DURATION_DAYS}
                loading={selectedId === pkg.id}
                disabled={isPending}
                onSelect={() => void handleSelect(pkg)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

// Baner soft-gate: Boost bez aktywnej subskrypcji nie ma efektu (profil ukryty), ale zakup
// jest dozwolony (backend go przyjmie). Kierujemy ekipę najpierw po pakiet ogłoszenia.
function NoSubscriptionBanner() {
  return (
    <div className="mx-auto mb-8 flex max-w-2xl items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
      <span
        className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-600"
        aria-hidden
      >
        <AlertCircleIcon />
      </span>
      <div className="flex-1">
        <span className="font-semibold">Twój profil nie jest jeszcze widoczny.</span> Boost podbija
        pozycję tylko aktywnego profilu.{' '}
        <Link
          to="/ekipa/pakiety"
          className="font-semibold underline underline-offset-2 hover:text-amber-900"
        >
          Wykup najpierw pakiet ogłoszenia
        </Link>
        , aby Boost przyniósł efekt.
      </div>
    </div>
  )
}

interface BoostCardProps {
  pkg: BoostPackage
  recommended: boolean
  loading: boolean
  disabled: boolean
  onSelect: () => void
}

function BoostCard({ pkg, recommended, loading, disabled, onSelect }: BoostCardProps) {
  const perDay = pkg.pricePln / pkg.durationDays

  return (
    <div
      className={[
        'relative flex flex-col rounded-xl border bg-surface-card p-6 shadow-sm transition-all duration-200',
        recommended
          ? 'border-brand-500 ring-2 ring-brand-500/50 sm:-translate-y-2'
          : 'border-navy-100 hover:-translate-y-0.5 hover:border-navy-200 hover:shadow-md',
      ].join(' ')}
    >
      {recommended && (
        <span className="absolute -top-3 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-brand-500 px-3 py-1 text-xs font-semibold text-white shadow-sm">
          ★ Najczęściej wybierany
        </span>
      )}

      {/* Nazwa pakietu */}
      <h2 className="text-lg font-bold text-navy-900">{pkg.name}</h2>
      <p className="mt-1 text-sm text-navy-600">
        Wyższe pozycjonowanie przez {pkg.durationDays} dni
      </p>

      {/* Cena */}
      <div className="mt-5 flex items-baseline gap-1">
        <span className="text-3xl font-bold tracking-tight text-navy-900">
          {formatPrice(pkg.pricePln)}
        </span>
      </div>
      <p className="mt-1 text-xs text-muted">ok. {formatPrice(perDay)} / dzień</p>

      {/* CTA — pchamy na dół karty, by przyciski były w jednej linii */}
      <button
        type="button"
        onClick={onSelect}
        disabled={disabled}
        aria-busy={loading}
        className={[
          'mt-6 inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60',
          recommended
            ? 'bg-brand-500 text-white hover:bg-brand-600'
            : 'border border-navy-200 text-navy-800 hover:border-brand-500 hover:text-brand-600',
        ].join(' ')}
      >
        {loading ? (
          <>
            <Spinner />
            Przekierowanie…
          </>
        ) : (
          'Kup Boost'
        )}
      </button>
    </div>
  )
}

function LoadingState() {
  return (
    <div className="flex items-center justify-center py-20">
      <div
        role="status"
        aria-label="Ładowanie pakietów..."
        className="h-10 w-10 animate-spin rounded-full border-4 border-navy-100 border-t-brand-500"
      />
    </div>
  )
}

function ErrorState() {
  return (
    <div className="flex items-center justify-center py-20">
      <p className="text-sm text-red-600">
        Nie udało się załadować pakietów Boost. Odśwież stronę.
      </p>
    </div>
  )
}

function EmptyState() {
  return (
    <div className="flex items-center justify-center py-20">
      <p className="text-sm text-navy-600">
        Brak dostępnych pakietów Boost. Spróbuj ponownie za chwilę.
      </p>
    </div>
  )
}

// currentColor → spinner dziedziczy kolor tekstu przycisku (biały na CTA, navy na outline).
function Spinner() {
  return (
    <span
      className="h-4 w-4 animate-spin rounded-full border-2 border-current/30 border-t-current"
      aria-hidden="true"
    />
  )
}

function AlertCircleIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  )
}
