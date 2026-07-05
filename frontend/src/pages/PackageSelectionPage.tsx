import { useState } from 'react'
import { useListingPackages } from '../hooks/usePackages'
import { useInitiateListingPayment } from '../hooks/usePayments'
import type { ListingPackage } from '../types/package.types'
import { formatPrice } from '../utils/formatPrice'

// Pakiet 30-dniowy jest wyróżniany jako „polecany" (najlepszy stosunek ceny do czasu).
const RECOMMENDED_DURATION_DAYS = 30

export default function PackageSelectionPage() {
  const { packages, isLoading, error } = useListingPackages()
  const { initiateListingPayment, isPending } = useInitiateListingPayment()

  // Który pakiet aktualnie inicjuje płatność — isPending z mutacji jest globalny dla hooka,
  // więc lokalnie śledzimy klikniętą kartę, by pokazać spinner tylko na niej.
  const [selectedId, setSelectedId] = useState<string | null>(null)

  async function handleSelect(pkg: ListingPackage) {
    setSelectedId(pkg.id)
    try {
      // Sukces przekierowuje do bramki P24 (obsługa w hooku); błąd emituje toast w hooku.
      await initiateListingPayment(pkg.id)
    } catch {
      // Redirect się nie wykonał — odblokuj karty, by ekipa mogła spróbować ponownie.
      setSelectedId(null)
    }
  }

  return (
    <div className="min-h-full flex-1 bg-surface">
      <div className="mx-auto max-w-5xl px-4 py-12 sm:px-6 lg:px-8">
        {/* Nagłówek */}
        <header className="mx-auto mb-10 max-w-2xl text-center">
          <span className="mb-3 inline-block rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-brand-700">
            Pakiety ogłoszenia
          </span>
          <h1 className="text-3xl font-bold tracking-tight text-navy-900 sm:text-4xl">
            Wybierz pakiet i pokaż się klientom
          </h1>
          <p className="mt-3 text-base text-navy-600">
            Po opłaceniu pakietu Twój profil staje się widoczny w wyszukiwarce ekip przez wybrany
            okres. Płatność obsługuje Przelewy24.
          </p>
        </header>

        {isLoading && <LoadingState />}
        {!isLoading && error && <ErrorState />}
        {!isLoading && !error && packages.length === 0 && <EmptyState />}

        {!isLoading && !error && packages.length > 0 && (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {packages.map((pkg) => (
              <PackageCard
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

interface PackageCardProps {
  pkg: ListingPackage
  recommended: boolean
  loading: boolean
  disabled: boolean
  onSelect: () => void
}

function PackageCard({ pkg, recommended, loading, disabled, onSelect }: PackageCardProps) {
  const perDay = pkg.pricePln / pkg.durationDays

  return (
    <div
      className={[
        'relative flex flex-col rounded-xl border bg-surface-card p-6 shadow-sm transition-all duration-200',
        recommended
          ? 'border-brand-500 ring-2 ring-brand-500/50 lg:-translate-y-2'
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
      <p className="mt-1 text-sm text-navy-600">Widoczność przez {pkg.durationDays} dni</p>

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
          'Wybierz'
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
      <p className="text-sm text-red-600">Nie udało się załadować pakietów. Odśwież stronę.</p>
    </div>
  )
}

function EmptyState() {
  return (
    <div className="flex items-center justify-center py-20">
      <p className="text-sm text-navy-600">Brak dostępnych pakietów. Spróbuj ponownie za chwilę.</p>
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
