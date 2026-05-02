import { Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useMyCrewProfile } from '../hooks/useMyCrewProfile'
import { useMyPhotos } from '../hooks/usePhotos'
import CrewProfileForm from '../components/crew/CrewProfileForm'
import PhotoUpload from '../components/photo/PhotoUpload'

export default function CrewDashboardPage() {
  const { user } = useAuth()
  const { profile, isLoading, error, hasProfile, refetch } = useMyCrewProfile()
  const { photos, isLoading: photosLoading } = useMyPhotos()

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
        <p className="text-sm text-red-600">
          Nie udało się załadować profilu. Odśwież stronę.
        </p>
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
              {user?.email.charAt(0).toUpperCase()}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-navy-900">Panel ekipy</h1>
              <p className="text-sm text-muted">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Baner statusu widoczności profilu */}
        {profile && (
          <div
            className={[
              'mb-6 flex items-center gap-3 rounded-xl border px-4 py-3 text-sm',
              profile.visible
                ? 'border-green-200 bg-green-50 text-green-800'
                : 'border-amber-200 bg-amber-50 text-amber-800',
            ].join(' ')}
          >
            <span
              className={[
                'flex h-7 w-7 shrink-0 items-center justify-center rounded-full',
                profile.visible ? 'bg-green-100 text-green-600' : 'bg-amber-100 text-amber-600',
              ].join(' ')}
              aria-hidden
            >
              {profile.visible ? <CheckCircleIcon /> : <AlertCircleIcon />}
            </span>
            <div className="flex-1">
              {profile.visible ? (
                <>
                  <span className="font-semibold">Profil jest widoczny</span>
                  {' — '}
                  <Link
                    to={`/ekipy/${profile.slug}`}
                    className="underline underline-offset-2 hover:text-green-900"
                  >
                    Zobacz profil publiczny
                  </Link>
                </>
              ) : (
                <>
                  <span className="font-semibold">Profil jest niewidoczny</span>
                  {' — '}
                  aktywuj pakiet, aby pojawić się na liście ekip
                </>
              )}
            </div>
          </div>
        )}

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
          <div className="mt-6 rounded-xl border border-navy-100 bg-surface-card p-6 shadow-sm sm:p-8">
            <div className="mb-6">
              <h2 className="text-lg font-bold text-navy-900">Portfolio</h2>
              <p className="mt-1 text-sm text-navy-600">
                Dodaj zdjęcia swoich realizacji — pojawią się na profilu po akceptacji przez moderatora.
              </p>
            </div>
            <PhotoUpload currentCount={photos.length} disabled={photosLoading} />
          </div>
        )}
      </div>
    </div>
  )
}

function CheckCircleIcon() {
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
      <polyline points="9 12 11 14 15 10" />
    </svg>
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
