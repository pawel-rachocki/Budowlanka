import { Link } from 'react-router-dom'
import { useMySubscription } from '../../hooks/useMySubscription'
import { formatDate } from '../../utils/formatDate'
import { dayLabel, daysUntil } from '../../utils/daysUntil'

interface SubscriptionWidgetProps {
  // Slug profilu — potrzebny do linku „Zobacz profil publiczny" w stanie aktywnym.
  // Hook subskrypcji nie zwraca slugu, więc przekazujemy go z dashboardu.
  slug: string
}

/**
 * Widget subskrypcji na dashboardzie ekipy (E-06 / F7).
 *
 * Jedyne źródło prawdy o widoczności profilu — zastępuje wcześniejszy baner oparty na
 * profile.visible. Pokazuje aktywny pakiet + datę wygaśnięcia, status boosta oraz CTA
 * „Przedłuż" / „Kup Boost". Gdy brak aktywnej subskrypcji — wyraźny baner niewidoczności.
 */
export default function SubscriptionWidget({ slug }: SubscriptionWidgetProps) {
  const { subscription, isLoading, error, refetch } = useMySubscription()

  if (isLoading) return <LoadingState />
  if (error || !subscription) return <ErrorState onRetry={() => void refetch()} />

  if (!subscription.hasActiveSubscription) return <NoSubscriptionBanner />

  return (
    <ActiveState
      slug={slug}
      expiresAt={subscription.subscription?.expiresAt ?? null}
      boost={subscription.boost}
    />
  )
}

// Aktywna subskrypcja — profil jest widoczny w wyszukiwarce.
//
// Nie pokazujemy nazwy pakietu: od REM-164 kolejny zakup „stackuje" czas (max(now, expires) +
// duration), więc nazwa ostatniego pakietu (np. „30 dni") nie odpowiada realnemu oknu. Źródłem
// prawdy jest expiresAt — prezentujemy pozostałe dni + datę wygaśnięcia.
function ActiveState({
  slug,
  expiresAt,
  boost,
}: {
  slug: string
  expiresAt: string | null
  boost: { boostName: string; expiresAt: string } | null
}) {
  return (
    <div className="mb-6 rounded-xl border border-navy-100 bg-surface-card p-6 shadow-sm">
      {/* Pasek statusu widoczności */}
      <div className="flex items-start gap-3">
        <span
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-green-100 text-green-600"
          aria-hidden
        >
          <CheckCircleIcon />
        </span>
        <div className="min-w-0 flex-1">
          <p className="font-semibold text-navy-900">Profil jest widoczny</p>
          <p className="mt-0.5 text-sm text-navy-600">
            {expiresAt ? (
              <>
                Aktywny jeszcze{' '}
                <span className="font-semibold text-navy-800">
                  {daysUntil(expiresAt)} {dayLabel(daysUntil(expiresAt))}
                </span>{' '}
                — do {formatDate(expiresAt)}
              </>
            ) : (
              <>Twój pakiet jest aktywny.</>
            )}
          </p>
          <Link
            to={`/ekipy/${slug}`}
            className="mt-1 inline-block text-sm font-semibold text-brand-600 underline underline-offset-2 hover:text-brand-700"
          >
            Zobacz profil publiczny
          </Link>
        </div>
      </div>

      {/* Status boosta */}
      <div className="mt-4 flex items-center gap-3 rounded-lg bg-surface px-4 py-3 text-sm">
        <span
          className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-brand-50 text-brand-600"
          aria-hidden
        >
          <BoltIcon />
        </span>
        <div className="min-w-0 flex-1 text-navy-700">
          {boost ? (
            <>
              <span className="font-semibold text-navy-800">
                Boost aktywny — jeszcze {daysUntil(boost.expiresAt)}{' '}
                {dayLabel(daysUntil(boost.expiresAt))}
              </span>{' '}
              (wyższe pozycjonowanie do {formatDate(boost.expiresAt)})
            </>
          ) : (
            <>Boost nieaktywny — podbij pozycję profilu w wynikach wyszukiwania.</>
          )}
        </div>
      </div>

      {/* CTA */}
      <div className="mt-5 flex flex-col gap-3 sm:flex-row">
        <Link
          to="/ekipa/pakiety"
          className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
        >
          Przedłuż
        </Link>
        <Link
          to="/ekipa/boost"
          className="inline-flex items-center justify-center rounded-lg border border-navy-200 px-4 py-2.5 text-sm font-semibold text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
        >
          {boost ? 'Przedłuż Boost' : 'Kup Boost'}
        </Link>
      </div>
    </div>
  )
}

// Brak aktywnej subskrypcji — profil niewidoczny, wyraźne CTA do zakupu pakietu.
function NoSubscriptionBanner() {
  return (
    <div className="mb-6 rounded-xl border border-amber-200 bg-amber-50 p-6 shadow-sm">
      <div className="flex items-start gap-3">
        <span
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-600"
          aria-hidden
        >
          <AlertCircleIcon />
        </span>
        <div className="min-w-0 flex-1">
          <p className="font-semibold text-amber-900">Twój profil jest niewidoczny</p>
          <p className="mt-0.5 text-sm text-amber-800">
            Wykup pakiet, aby pojawić się w wyszukiwarce ekip i zbierać zapytania od klientów.
          </p>
          <Link
            to="/ekipa/pakiety"
            className="mt-4 inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
          >
            Wykup pakiet
          </Link>
        </div>
      </div>
    </div>
  )
}

function LoadingState() {
  return (
    <div className="mb-6 flex items-center justify-center rounded-xl border border-navy-100 bg-surface-card py-12 shadow-sm">
      <div
        role="status"
        aria-label="Ładowanie statusu subskrypcji..."
        className="h-8 w-8 animate-spin rounded-full border-4 border-navy-100 border-t-brand-500"
      />
    </div>
  )
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="mb-6 flex flex-col items-center gap-3 rounded-xl border border-navy-100 bg-surface-card px-6 py-8 text-center shadow-sm">
      <p className="text-sm text-navy-600">Nie udało się załadować statusu subskrypcji.</p>
      <button
        type="button"
        onClick={onRetry}
        className="inline-flex items-center justify-center rounded-lg border border-navy-200 px-4 py-2 text-sm font-semibold text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        Spróbuj ponownie
      </button>
    </div>
  )
}

function CheckCircleIcon() {
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
      <circle cx="12" cy="12" r="10" />
      <polyline points="9 12 11 14 15 10" />
    </svg>
  )
}

function AlertCircleIcon() {
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
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  )
}

function BoltIcon() {
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
      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
    </svg>
  )
}
