import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useMySubscription } from '../hooks/useMySubscription'
import { formatDate } from '../utils/formatDate'

// Aktywacja pakietu następuje asynchronicznie (webhook P24), więc po powrocie z bramki
// odpytujemy status subskrypcji co kilka sekund przez krótkie okno. Gdy webhook zaksięguje
// płatność, hasActiveSubscription zmieni się na true i pokażemy potwierdzenie.
const POLL_INTERVAL_MS = 3_000
const POLL_WINDOW_MS = 45_000

export default function PaymentSuccessPage() {
  const queryClient = useQueryClient()

  // Okno pollingu zamyka się po upływie POLL_WINDOW_MS (timeout ustawia windowClosed=true).
  const [windowClosed, setWindowClosed] = useState(false)

  const { subscription, refetch, isLoading } = useMySubscription({
    // Odpytujemy dopóki aktywacja nie potwierdzona i nie minęło okno oczekiwania.
    // Forma funkcyjna czyta bieżące dane zapytania, więc polling gaśnie sam po aktywacji.
    refetchInterval: (query) =>
      !windowClosed && query.state.data?.hasActiveSubscription !== true ? POLL_INTERVAL_MS : false,
  })

  const isActive = subscription?.hasActiveSubscription === true

  // Na wejściu unieważniamy cache subskrypcji i płatności — dane sprzed redirectu do P24
  // są nieaktualne. Po POLL_WINDOW_MS zamykamy okno pollingu niezależnie od wyniku.
  useEffect(() => {
    void queryClient.invalidateQueries({ queryKey: ['subscription', 'me'] })
    void queryClient.invalidateQueries({ queryKey: ['payments', 'me'] })

    const timer = setTimeout(() => setWindowClosed(true), POLL_WINDOW_MS)
    return () => clearTimeout(timer)
  }, [queryClient])

  return (
    <div className="min-h-full flex-1 bg-surface">
      <div className="mx-auto flex max-w-lg flex-col items-center px-4 py-16 text-center sm:px-6 lg:py-24">
        {isActive ? (
          <ConfirmedState
            packageName={subscription?.subscription?.packageName ?? null}
            expiresAt={subscription?.subscription?.expiresAt ?? null}
          />
        ) : !windowClosed ? (
          <PendingState />
        ) : (
          <TimeoutState onRefresh={() => void refetch()} refreshing={isLoading} />
        )}
      </div>
    </div>
  )
}

// Aktywacja potwierdzona przez webhook — profil jest już widoczny.
function ConfirmedState({
  packageName,
  expiresAt,
}: {
  packageName: string | null
  expiresAt: string | null
}) {
  return (
    <>
      <span
        className="flex h-16 w-16 items-center justify-center rounded-full bg-green-100 text-green-600"
        aria-hidden
      >
        <CheckCircleIcon />
      </span>
      <h1 className="mt-6 text-2xl font-bold tracking-tight text-navy-900 sm:text-3xl">
        Płatność zaksięgowana
      </h1>
      <p className="mt-3 text-base text-navy-600">
        {packageName ? (
          <>
            Pakiet <span className="font-semibold text-navy-800">{packageName}</span> jest aktywny
            {expiresAt && <> do {formatDate(expiresAt)}</>}. Twój profil jest teraz widoczny w
            wyszukiwarce ekip.
          </>
        ) : (
          <>Pakiet został aktywowany. Twój profil jest teraz widoczny w wyszukiwarce ekip.</>
        )}
      </p>
      <Actions />
    </>
  )
}

// Płatność wraca z P24 zanim webhook zdąży ją zaksięgować — czekamy i odpytujemy status.
function PendingState() {
  return (
    <>
      <span
        className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-50 text-brand-600"
        aria-hidden
      >
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-200 border-t-brand-500" />
      </span>
      <h1 className="mt-6 text-2xl font-bold tracking-tight text-navy-900 sm:text-3xl">
        Dziękujemy za płatność
      </h1>
      <p className="mt-3 text-base text-navy-600">
        Przetwarzamy Twoją płatność — aktywacja pakietu może potrwać kilka chwil. Ta strona
        zaktualizuje się automatycznie, gdy potwierdzimy transakcję.
      </p>
    </>
  )
}

// Minęło okno oczekiwania, a status wciąż nieaktywny — płatność mogła jeszcze się nie zaksięgować.
function TimeoutState({ onRefresh, refreshing }: { onRefresh: () => void; refreshing: boolean }) {
  return (
    <>
      <span
        className="flex h-16 w-16 items-center justify-center rounded-full bg-amber-100 text-amber-600"
        aria-hidden
      >
        <ClockIcon />
      </span>
      <h1 className="mt-6 text-2xl font-bold tracking-tight text-navy-900 sm:text-3xl">
        Płatność w toku
      </h1>
      <p className="mt-3 text-base text-navy-600">
        Twoja płatność mogła jeszcze się nie zaksięgować. Zwykle trwa to tylko chwilę — odśwież
        status za moment albo sprawdź go później w panelu.
      </p>
      <button
        type="button"
        onClick={onRefresh}
        disabled={refreshing}
        aria-busy={refreshing}
        className="mt-6 inline-flex items-center justify-center gap-2 rounded-lg border border-navy-200 px-4 py-2.5 text-sm font-semibold text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {refreshing && (
          <span
            className="h-4 w-4 animate-spin rounded-full border-2 border-current/30 border-t-current"
            aria-hidden="true"
          />
        )}
        Odśwież status
      </button>
      <Actions />
    </>
  )
}

// Wspólne CTA pod komunikatem — powrót do panelu i do katalogu pakietów.
function Actions() {
  return (
    <div className="mt-8 flex flex-col gap-3 sm:flex-row">
      <Link
        to="/dashboard"
        className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        Przejdź do panelu
      </Link>
      <Link
        to="/ekipa/pakiety"
        className="inline-flex items-center justify-center rounded-lg border border-navy-200 px-5 py-2.5 text-sm font-semibold text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        Zobacz pakiety
      </Link>
    </div>
  )
}

function CheckCircleIcon() {
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
      <circle cx="12" cy="12" r="10" />
      <polyline points="9 12 11 14 15 10" />
    </svg>
  )
}

function ClockIcon() {
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
      <circle cx="12" cy="12" r="10" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  )
}
