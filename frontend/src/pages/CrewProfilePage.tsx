import type { ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import PhotoGallery from '../components/photo/PhotoGallery'
import { useAuth } from '../hooks/useAuth'
import { useCrewProfile } from '../hooks/useCrewProfile'
import { useCrewPhotos } from '../hooks/usePhotos'
import { VOIVODESHIP_LABELS } from '../utils/voivodeships'

// ── Helpers ───────────────────────────────────────────────────────────────────

function pluralOpinii(count: number): string {
  if (count === 1) return 'opinia'
  const mod10 = count % 10
  const mod100 = count % 100
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 'opinie'
  return 'opinii'
}

// ── Main page ─────────────────────────────────────────────────────────────────

export default function CrewProfilePage() {
  const { slug } = useParams<{ slug: string }>()
  const { profile, isLoading, error, refetch } = useCrewProfile(slug)
  const { user, isLoading: authLoading } = useAuth()
  const { photos, isLoading: photosLoading } = useCrewPhotos(profile ? slug : undefined)

  const isLoggedIn = user !== null
  const is404 = error?.response?.status === 404

  if (isLoading) return <SkeletonLayout />
  if (error && is404) return <NotFoundState />
  if (error) return <ErrorState onRetry={() => void refetch()} />
  if (!profile) return null

  const voivodeshipLabel = VOIVODESHIP_LABELS[profile.voivodeship]

  return (
    <>
      {/* ── Header ──────────────────────────────────────────────────────── */}
      <header className="relative overflow-hidden bg-surface-card border-b border-navy-100">
        {/* Blueprint grid */}
        <div
          className="absolute inset-0 pointer-events-none"
          aria-hidden="true"
          style={{
            backgroundImage: [
              'repeating-linear-gradient(0deg, transparent, transparent 39px, rgba(45,90,142,0.03) 39px, rgba(45,90,142,0.03) 40px)',
              'repeating-linear-gradient(90deg, transparent, transparent 39px, rgba(45,90,142,0.03) 39px, rgba(45,90,142,0.03) 40px)',
            ].join(', '),
          }}
        />
        {/* Warm glow */}
        <div
          className="absolute -top-20 -right-20 w-72 h-72 rounded-full bg-brand-500/[0.04] blur-3xl pointer-events-none"
          aria-hidden="true"
        />

        <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 sm:py-14">
          {/* Breadcrumb */}
          <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 mb-3 flex-wrap">
            <Link to="/" className="text-xs text-muted hover:text-navy-600 transition-colors">
              Portal
            </Link>
            <ChevronIcon />
            <Link
              to="/ekipy"
              className="text-xs text-muted hover:text-navy-600 transition-colors"
            >
              Ekipy remontowe
            </Link>
            <ChevronIcon />
            <span
              className="text-xs font-medium text-navy-600 truncate max-w-[200px]"
              aria-current="page"
            >
              {profile.companyName}
            </span>
          </nav>

          {/* Company name */}
          <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-navy-900">
            {profile.companyName}
          </h1>

          {/* Meta badges */}
          <div className="mt-4 flex flex-wrap gap-2">
            <MetaBadge icon={<LocationIcon />}>
              {profile.city}, {voivodeshipLabel}
            </MetaBadge>
            {profile.serviceRadiusKm != null && (
              <MetaBadge icon={<RadiusIcon />}>Zasięg {profile.serviceRadiusKm} km</MetaBadge>
            )}
          </div>
        </div>
      </header>

      {/* ── Main content ────────────────────────────────────────────────── */}
      <section className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8">
          {/* ── Left: content ─────────────────────────────────────────── */}
          <div className="md:col-span-2 flex flex-col gap-5">
            {/* Rating */}
            <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
              <h2 className="text-xs font-semibold uppercase tracking-wider text-muted mb-3">
                Ocena
              </h2>
              {profile.reviewCount === 0 ? (
                <div className="flex items-center gap-3 flex-wrap">
                  <StarRating rating={0} />
                  <span className="text-sm text-muted">
                    Brak ocen — pierwsze opinie pojawią się wkrótce
                  </span>
                </div>
              ) : (
                <div className="flex items-center gap-3 flex-wrap">
                  <StarRating rating={profile.avgRating} />
                  <span
                    className="text-lg font-bold text-navy-900"
                    aria-label={`Ocena ${profile.avgRating.toFixed(1)} na 5`}
                  >
                    {profile.avgRating.toFixed(1)}
                    <span className="text-sm font-normal text-muted"> / 5</span>
                  </span>
                  <span className="text-sm text-navy-600">
                    ({profile.reviewCount} {pluralOpinii(profile.reviewCount)})
                  </span>
                </div>
              )}
            </div>

            {/* Categories */}
            <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
              <h2 className="text-xs font-semibold uppercase tracking-wider text-muted mb-3">
                Specjalizacje
              </h2>
              {profile.serviceCategories.length === 0 ? (
                <p className="text-sm text-muted italic">Brak przypisanych kategorii usług.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {profile.serviceCategories.map((cat) => (
                    <span
                      key={cat.id}
                      className="inline-block rounded-full bg-brand-50 px-3 py-1 text-sm font-medium text-brand-700"
                    >
                      {cat.name}
                    </span>
                  ))}
                </div>
              )}
            </div>

            {/* Description */}
            <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
              <h2 className="text-xs font-semibold uppercase tracking-wider text-muted mb-3">
                O nas
              </h2>
              {profile.description ? (
                <p className="text-sm leading-relaxed text-navy-700 whitespace-pre-wrap">
                  {profile.description}
                </p>
              ) : (
                <p className="text-sm text-muted italic">
                  Ekipa nie dodała jeszcze opisu swojej działalności.
                </p>
              )}
            </div>
          </div>

          {/* ── Right: sidebar ────────────────────────────────────────── */}
          <div className="md:col-span-1">
            <div className="sticky top-24">
              {authLoading ? (
                <div className="rounded-xl border border-navy-100 bg-surface-card p-5 h-44 animate-pulse" />
              ) : (
                <ContactCard
                  isLoggedIn={isLoggedIn}
                  phone={profile.phone}
                  email={profile.contactEmail}
                />
              )}
            </div>
          </div>
        </div>
      </section>

      {/* ── Portfolio gallery ────────────────────────────────────────────── */}
      {(photos.length > 0 || photosLoading) && (
        <section
          aria-label="Portfolio zdjęć"
          className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pb-10"
        >
          <PhotoGallery photos={photos} isLoading={photosLoading} />
        </section>
      )}
    </>
  )
}

// ── Contact card ──────────────────────────────────────────────────────────────

function ContactCard({
  isLoggedIn,
  phone,
  email,
}: {
  isLoggedIn: boolean
  phone: string | null
  email: string | null
}) {
  return (
    <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
      <h2 className="text-xs font-semibold uppercase tracking-wider text-muted mb-4">
        Dane kontaktowe
      </h2>
      <ContactCardBody isLoggedIn={isLoggedIn} phone={phone} email={email} />
    </div>
  )
}

function ContactCardBody({
  isLoggedIn,
  phone,
  email,
}: {
  isLoggedIn: boolean
  phone: string | null
  email: string | null
}) {
  if (!isLoggedIn) {
    return (
      <div className="flex flex-col items-center gap-4 py-2 text-center">
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-navy-50">
          <LockIcon />
        </div>
        <p className="text-sm text-navy-600 leading-relaxed">
          Zaloguj się, aby zobaczyć dane kontaktowe ekipy.
        </p>
        <Link
          to="/login"
          className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
        >
          Zaloguj się
        </Link>
      </div>
    )
  }

  if (phone == null && email == null) {
    return (
      <p className="text-sm text-muted italic">Ekipa nie podała danych kontaktowych.</p>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {phone != null && (
        <a
          href={`tel:${phone}`}
          className="flex items-center gap-3 rounded-lg border border-navy-100 px-4 py-3 text-sm font-medium text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          <PhoneIcon />
          {phone}
        </a>
      )}
      {email != null && (
        <a
          href={`mailto:${email}`}
          className="flex items-center gap-3 rounded-lg border border-navy-100 px-4 py-3 text-sm font-medium text-navy-800 transition-colors hover:border-brand-500 hover:text-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
        >
          <EmailIcon />
          <span className="truncate">{email}</span>
        </a>
      )}
    </div>
  )
}

// ── Star rating ───────────────────────────────────────────────────────────────

function StarRating({ rating }: { rating: number }) {
  const filled = Math.round(rating)
  const label = rating === 0 ? 'Brak ocen' : `Ocena ${rating.toFixed(1)} na 5`
  return (
    <div
      className="flex gap-0.5"
      aria-label={label}
      role="img"
    >
      {Array.from({ length: 5 }).map((_, i) => (
        <svg
          key={i}
          viewBox="0 0 16 16"
          fill="currentColor"
          className={`h-5 w-5 ${i < filled ? 'text-brand-500' : 'text-navy-100'}`}
          aria-hidden="true"
        >
          <path d="M8 .25a.75.75 0 01.673.418l1.882 3.815 4.21.612a.75.75 0 01.416 1.279l-3.046 2.97.719 4.192a.75.75 0 01-1.088.791L8 12.347l-3.766 1.98a.75.75 0 01-1.088-.79l.72-4.194L.818 6.374a.75.75 0 01.416-1.28l4.21-.611L7.327.668A.75.75 0 018 .25z" />
        </svg>
      ))}
    </div>
  )
}

// ── Skeleton layout ───────────────────────────────────────────────────────────

function SkeletonLayout() {
  return (
    <>
      {/* Header skeleton */}
      <div className="bg-surface-card border-b border-navy-100 px-4 sm:px-6 lg:px-8 py-10 sm:py-14">
        <div className="max-w-4xl mx-auto animate-pulse">
          {/* Breadcrumb */}
          <div className="flex items-center gap-2 mb-4">
            <div className="h-3 w-10 rounded bg-navy-100" />
            <div className="h-3 w-3 rounded bg-navy-100" />
            <div className="h-3 w-24 rounded bg-navy-100" />
            <div className="h-3 w-3 rounded bg-navy-100" />
            <div className="h-3 w-36 rounded bg-navy-100" />
          </div>
          {/* Title */}
          <div className="h-10 w-2/3 rounded-lg bg-navy-100 mb-4" />
          {/* Meta badges */}
          <div className="flex gap-2">
            <div className="h-7 w-40 rounded-full bg-navy-100" />
            <div className="h-7 w-28 rounded-full bg-navy-100" />
          </div>
        </div>
      </div>
      {/* Main skeleton */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 animate-pulse">
          {/* Left */}
          <div className="md:col-span-2 flex flex-col gap-5">
            {/* Rating card */}
            <div className="rounded-xl border border-navy-100 bg-surface-card p-5">
              <div className="h-3 w-10 rounded bg-navy-100 mb-3" />
              <div className="flex gap-1">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="h-5 w-5 rounded bg-navy-100" />
                ))}
                <div className="ml-2 h-5 w-16 rounded bg-navy-100" />
              </div>
            </div>
            {/* Categories card */}
            <div className="rounded-xl border border-navy-100 bg-surface-card p-5">
              <div className="h-3 w-20 rounded bg-navy-100 mb-3" />
              <div className="flex gap-2 flex-wrap">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="h-6 w-20 rounded-full bg-navy-100" />
                ))}
              </div>
            </div>
            {/* Description card */}
            <div className="rounded-xl border border-navy-100 bg-surface-card p-5">
              <div className="h-3 w-12 rounded bg-navy-100 mb-3" />
              <div className="space-y-2">
                <div className="h-3 w-full rounded bg-navy-100" />
                <div className="h-3 w-5/6 rounded bg-navy-100" />
                <div className="h-3 w-4/6 rounded bg-navy-100" />
                <div className="h-3 w-3/4 rounded bg-navy-100" />
              </div>
            </div>
          </div>
          {/* Right sidebar */}
          <div className="md:col-span-1">
            <div className="rounded-xl border border-navy-100 bg-surface-card p-5 h-52" />
          </div>
        </div>
      </div>
      {/* Gallery skeleton */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pb-10 animate-pulse">
        <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
          <div className="h-3 w-16 rounded bg-navy-100 mb-4" />
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="aspect-square rounded-lg bg-navy-100" />
            ))}
          </div>
        </div>
      </div>
    </>
  )
}

// ── 404 state ─────────────────────────────────────────────────────────────────

function NotFoundState() {
  return (
    <div className="flex flex-col items-center justify-center py-24 px-4 text-center">
      <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-2xl bg-navy-50">
        <HardHatIcon />
      </div>
      <h1 className="text-xl font-bold text-navy-900">Nie znaleziono ekipy</h1>
      <p className="mt-2 max-w-sm text-sm leading-relaxed text-navy-600">
        Profil o podanym adresie nie istnieje lub nie jest dostępny.
      </p>
      <Link
        to="/ekipy"
        className="mt-8 inline-flex items-center gap-2 rounded-lg border border-navy-100 bg-surface-card px-5 py-2.5 text-sm font-medium text-navy-700 shadow-sm transition-colors hover:border-navy-200 hover:bg-navy-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        <svg
          viewBox="0 0 16 16"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.75"
          className="h-4 w-4"
          aria-hidden="true"
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M10 13L5 8l5-5" />
        </svg>
        Wróć do listy ekip
      </Link>
    </div>
  )
}

// ── Error state ───────────────────────────────────────────────────────────────

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-24 px-4 text-center">
      <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-navy-50">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="h-8 w-8 text-navy-600"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>
      <h1 className="text-xl font-bold text-navy-900">Błąd ładowania profilu</h1>
      <p className="mt-2 max-w-sm text-sm leading-relaxed text-navy-600">
        Wystąpił problem z połączeniem. Sprawdź internet i spróbuj ponownie.
      </p>
      <button
        onClick={onRetry}
        className="mt-8 inline-flex items-center gap-2 rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="h-4 w-4"
          aria-hidden="true"
        >
          <path d="M1 4v6h6" />
          <path d="M23 20v-6h-6" />
          <path d="M20.49 9A9 9 0 005.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 013.51 15" />
        </svg>
        Spróbuj ponownie
      </button>
    </div>
  )
}

// ── Small icon components ─────────────────────────────────────────────────────

function ChevronIcon() {
  return (
    <svg
      width="12"
      height="12"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="text-muted"
      aria-hidden="true"
    >
      <polyline points="9 18 15 12 9 6" />
    </svg>
  )
}

function LocationIcon() {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="currentColor"
      className="h-3.5 w-3.5 shrink-0 text-muted"
      aria-hidden="true"
    >
      <path
        fillRule="evenodd"
        d="M8 1a4.5 4.5 0 00-4.5 4.5C3.5 8.75 8 15 8 15s4.5-6.25 4.5-9.5A4.5 4.5 0 008 1zm0 6a1.5 1.5 0 110-3 1.5 1.5 0 010 3z"
        clipRule="evenodd"
      />
    </svg>
  )
}

function RadiusIcon() {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      className="h-3.5 w-3.5 shrink-0 text-muted"
      aria-hidden="true"
    >
      <circle cx="8" cy="8" r="5.5" strokeDasharray="2 2" />
      <circle cx="8" cy="8" r="1.5" fill="currentColor" stroke="none" />
      <line x1="8" y1="8" x2="12.5" y2="8" />
    </svg>
  )
}

function PhoneIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-4 w-4 shrink-0 text-muted"
      aria-hidden="true"
    >
      <path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.69A2 2 0 012 0h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L6.09 7.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 14v2.92z" />
    </svg>
  )
}

function EmailIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-4 w-4 shrink-0 text-muted"
      aria-hidden="true"
    >
      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
      <polyline points="22,6 12,13 2,6" />
    </svg>
  )
}

function LockIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-6 w-6 text-muted"
      aria-hidden="true"
    >
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0110 0v4" />
    </svg>
  )
}

function HardHatIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-10 w-10 text-navy-600"
      aria-hidden="true"
    >
      <path d="M2 18a1 1 0 001 1h18a1 1 0 001-1v-2a1 1 0 00-1-1H3a1 1 0 00-1 1v2z" />
      <path d="M10 10V7a2 2 0 114 0v3" />
      <path d="M4 15v-3a8 8 0 1116 0v3" />
    </svg>
  )
}

interface MetaBadgeProps {
  icon: ReactNode
  children: ReactNode
}

function MetaBadge({ icon, children }: MetaBadgeProps) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-navy-100 bg-surface-card px-3 py-1 text-sm text-navy-700 shadow-sm">
      {icon}
      {children}
    </span>
  )
}
