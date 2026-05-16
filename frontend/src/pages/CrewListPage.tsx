import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import CrewCard from '../components/crew/CrewCard'
import CrewCardSkeleton from '../components/crew/CrewCardSkeleton'
import CrewFilters, { type CrewFiltersValue } from '../components/crew/CrewFilters'
import Pagination from '../components/Pagination'
import { useCrews } from '../hooks/useCrews'
import type { Voivodeship } from '../types/crew.types'
import { VOIVODESHIPS_ORDERED } from '../utils/voivodeships'

const SKELETON_COUNT = 6

function formatCrewCount(count: number): string {
  if (count === 1) return '1 ekipa'
  const mod10 = count % 10
  const mod100 = count % 100
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return `${count} ekipy`
  return `${count} ekip`
}

export default function CrewListPage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const urlCity = searchParams.get('city') ?? ''
  const rawVoivodeship = searchParams.get('voivodeship')
  const urlVoivodeship: Voivodeship | undefined =
    rawVoivodeship !== null && (VOIVODESHIPS_ORDERED as readonly string[]).includes(rawVoivodeship)
      ? (rawVoivodeship as Voivodeship)
      : undefined
  const urlCategoryId = searchParams.get('categoryId') || undefined
  const urlPage = Math.max(0, parseInt(searchParams.get('page') ?? '0', 10) || 0)

  // Local state for city input — debounced to URL
  const [cityInput, setCityInput] = useState(urlCity)
  const isMounted = useRef(false)

  // Debounce city input → URL (skip first render to avoid resetting page on mount)
  useEffect(() => {
    if (!isMounted.current) {
      isMounted.current = true
      return
    }
    const timer = setTimeout(() => {
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev)
          if (cityInput) next.set('city', cityInput)
          else next.delete('city')
          next.delete('page')
          return next
        },
        { replace: true }
      )
    }, 400)
    return () => clearTimeout(timer)
  }, [cityInput, setSearchParams])

  // Scroll to top when page changes
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, [urlPage])

  const { crews, totalElements, totalPages, isLoading, isFetching, error, refetch } = useCrews({
    city: urlCity || undefined,
    voivodeship: urlVoivodeship,
    categoryId: urlCategoryId,
    page: urlPage,
    size: 20,
  })

  const filtersValue: CrewFiltersValue = {
    city: cityInput,
    voivodeship: urlVoivodeship,
    categoryId: urlCategoryId,
  }

  const handleFiltersChange = useCallback(
    (next: CrewFiltersValue) => {
      setCityInput(next.city)
      const voivodeshipChanged = next.voivodeship !== urlVoivodeship
      const categoryChanged = next.categoryId !== urlCategoryId
      if (voivodeshipChanged || categoryChanged) {
        setSearchParams(
          (prev) => {
            const params = new URLSearchParams(prev)
            if (next.voivodeship) params.set('voivodeship', next.voivodeship)
            else params.delete('voivodeship')
            if (next.categoryId) params.set('categoryId', next.categoryId)
            else params.delete('categoryId')
            params.delete('page')
            return params
          },
          { replace: true }
        )
      }
    },
    [urlVoivodeship, urlCategoryId, setSearchParams]
  )

  const handleReset = useCallback(() => {
    setCityInput('')
    setSearchParams(new URLSearchParams(), { replace: true })
  }, [setSearchParams])

  const handlePageChange = useCallback(
    (page: number) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        if (page === 0) next.delete('page')
        else next.set('page', String(page))
        return next
      })
    },
    [setSearchParams]
  )

  const showGrid = !error
  const showSkeletons = isLoading
  const showFetchingOverlay = isFetching && !isLoading
  const showEmpty = !isLoading && !error && crews.length === 0

  return (
    <>
      {/* ── Page header ───────────────────────────────────────────── */}
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

        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 sm:py-14">
          {/* Breadcrumb */}
          <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 mb-3">
            <Link to="/" className="text-xs text-muted hover:text-navy-600 transition-colors">
              Portal
            </Link>
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
            <span className="text-xs font-medium text-navy-600" aria-current="page">
              Ekipy remontowe
            </span>
          </nav>

          <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-navy-900">
            Ekipy remontowe
          </h1>
          <p className="mt-2 text-base text-navy-600 max-w-lg">
            Sprawdzone ekipy z całej Polski&nbsp;&mdash; filtruj po lokalizacji i specjalizacji.
          </p>
        </div>
      </header>

      {/* ── Sticky filter bar ─────────────────────────────────────── */}
      <div className="sticky top-16 z-10 bg-surface/90 backdrop-blur-sm border-b border-navy-100 py-4">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <CrewFilters value={filtersValue} onChange={handleFiltersChange} onReset={handleReset} />
        </div>
      </div>

      {/* ── Results section ───────────────────────────────────────── */}
      <section className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Results status bar */}
        <div
          className="flex items-center gap-2.5 mb-6 min-h-7"
          aria-live="polite"
          aria-atomic="true"
        >
          {isLoading ? null : isFetching ? (
            <>
              <SpinnerIcon />
              <span className="text-sm text-navy-600">Wyszukiwanie&hellip;</span>
            </>
          ) : !error ? (
            <p className="text-sm font-medium text-navy-700">
              Znaleziono{' '}
              <span className="text-brand-500 font-bold">{formatCrewCount(totalElements)}</span>
            </p>
          ) : null}
        </div>

        {/* Error state */}
        {error && <ErrorState onRetry={refetch} />}

        {/* Crew grid */}
        {showGrid && (
          <div className={showFetchingOverlay ? 'opacity-60 transition-opacity duration-150' : ''}>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
              {showSkeletons
                ? Array.from({ length: SKELETON_COUNT }).map((_, i) => <CrewCardSkeleton key={i} />)
                : crews.map((crew) => <CrewCard key={crew.id} crew={crew} />)}
            </div>

            {showEmpty && <EmptyState />}
          </div>
        )}

        {/* Pagination */}
        {!isLoading && !error && totalPages > 1 && (
          <Pagination
            currentPage={urlPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            disabled={isFetching}
          />
        )}
      </section>
    </>
  )
}

// ── Sub-components ────────────────────────────────────────────────────────────

function SpinnerIcon() {
  return (
    <svg
      className="h-4 w-4 animate-spin text-brand-500 shrink-0"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
      />
    </svg>
  )
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      {/* Tool illustration */}
      <div className="relative mb-6">
        <div className="w-20 h-20 rounded-2xl bg-brand-50 flex items-center justify-center">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none" aria-hidden="true">
            {/* Wrench */}
            <path
              d="M28 6a6 6 0 0 0-5.93 7.07L8.5 26.64A2.5 2.5 0 1 0 12 30l13.57-13.57A6 6 0 1 0 28 6z"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="text-brand-500"
            />
            {/* Magnifier */}
            <circle
              cx="30"
              cy="30"
              r="6"
              stroke="currentColor"
              strokeWidth="2"
              strokeOpacity="0.4"
              className="text-navy-600"
            />
            <path
              d="M34.5 34.5 L38 38"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeOpacity="0.4"
              className="text-navy-600"
            />
            {/* Small sparkle */}
            <path
              d="M8 8 L8 12 M6 10 L10 10"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeOpacity="0.4"
              className="text-brand-500"
            />
          </svg>
        </div>
        <div
          className="absolute -bottom-1 -right-1 w-6 h-6 rounded-full border-2 border-surface bg-navy-50 flex items-center justify-center"
          aria-hidden="true"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M18 6L6 18M6 6l12 12"
              stroke="#8ba0b4"
              strokeWidth="2.5"
              strokeLinecap="round"
            />
          </svg>
        </div>
      </div>

      <h2 className="text-lg font-bold text-navy-900">Brak ekip spełniających kryteria</h2>
      <p className="mt-2 text-sm text-navy-600 max-w-xs leading-relaxed">
        Spróbuj zmienić filtry — inne miasto, województwo lub kategorię usług.
      </p>
    </div>
  )
}

function ErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-16 h-16 rounded-2xl bg-navy-50 flex items-center justify-center mb-5">
        <svg
          width="32"
          height="32"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="text-navy-600"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>
      <h2 className="text-lg font-bold text-navy-900">Nie udało się załadować ekip</h2>
      <p className="mt-2 text-sm text-navy-600 max-w-xs leading-relaxed">
        Wystąpił problem z połączeniem. Sprawdź internet i spróbuj ponownie.
      </p>
      <button
        onClick={onRetry}
        className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold text-white bg-brand-500 hover:bg-brand-600 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
      >
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
          <path d="M1 4v6h6" />
          <path d="M23 20v-6h-6" />
          <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" />
        </svg>
        Spróbuj ponownie
      </button>
    </div>
  )
}
