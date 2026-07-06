import { useAuth } from '../hooks/useAuth'
import { useMyCrewProfile } from '../hooks/useMyCrewProfile'
import CrewProfileForm from '../components/crew/CrewProfileForm'
import PortfolioManager from '../components/photo/PortfolioManager'
import SubscriptionWidget from '../components/crew/SubscriptionWidget'

export default function CrewDashboardPage() {
  const { user } = useAuth()
  const { profile, isLoading, error, hasProfile, refetch } = useMyCrewProfile()

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center bg-surface py-20">
        <div
          role="status"
          aria-label="Ładowanie..."
          className="h-10 w-10 animate-spin rounded-full border-4 border-navy-100 border-t-brand-500"
        />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-1 items-center justify-center bg-surface py-20">
        <p className="text-sm text-red-600">Nie udało się załadować profilu. Odśwież stronę.</p>
      </div>
    )
  }

  return (
    <div className="min-h-full flex-1 bg-surface">
      <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6 lg:px-8">
        {/* Nagłówek */}
        <div className="mb-8">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-500 text-base font-bold text-white">
              {user?.email?.[0]?.toUpperCase()}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-navy-900">Panel ekipy</h1>
              <p className="text-sm text-muted">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Widget subskrypcji: status widoczności, pakiet, boost, CTA (F7) */}
        {profile && <SubscriptionWidget slug={profile.slug} />}

        {/* Karta z formularzem */}
        <div className="rounded-xl border border-navy-100 bg-surface-card p-6 shadow-sm sm:p-8">
          <div className="mb-6">
            <h2 className="text-lg font-bold text-navy-900">
              {hasProfile ? 'Edytuj profil' : 'Utwórz profil ekipy'}
            </h2>
            <p className="mt-1 text-sm text-navy-600">
              {hasProfile
                ? 'Zaktualizuj informacje o swojej firmie.'
                : 'Uzupełnij dane, aby klienci mogli Cię znaleźć.'}
            </p>
          </div>

          <CrewProfileForm profile={profile} onSuccess={() => void refetch()} />
        </div>

        {/* Portfolio */}
        {hasProfile && (
          <div className="mt-6">
            <PortfolioManager />
          </div>
        )}
      </div>
    </div>
  )
}
