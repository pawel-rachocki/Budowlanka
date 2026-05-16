import { Link } from 'react-router-dom'
import type { CrewProfileSummaryResponse } from '../../types/crew.types'
import { VOIVODESHIP_LABELS } from '../../utils/voivodeships'

interface CrewCardProps {
  crew: CrewProfileSummaryResponse
}

const MAX_VISIBLE_CATEGORIES = 3

function pluralOpinii(count: number): string {
  if (count === 1) return 'opinia'
  const mod10 = count % 10
  const mod100 = count % 100
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 'opinie'
  return 'opinii'
}

export default function CrewCard({ crew }: CrewCardProps) {
  const visibleCategories = crew.serviceCategories.slice(0, MAX_VISIBLE_CATEGORIES)
  const overflowCount = Math.max(0, crew.serviceCategories.length - MAX_VISIBLE_CATEGORIES)

  return (
    <Link
      to={`/ekipy/${encodeURIComponent(crew.slug)}`}
      className="group relative block overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md hover:border-navy-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
    >
      {/* Left accent strip */}
      <div className="absolute inset-y-0 left-0 w-1 bg-brand-500" aria-hidden="true" />

      <div className="flex flex-col gap-3 py-5 pr-5 pl-6 sm:py-6 sm:pr-6 sm:pl-7">
        {/* Company name */}
        <h3 className="line-clamp-2 text-base font-bold leading-tight text-navy-900 transition-colors group-hover:text-brand-600">
          {crew.companyName}
        </h3>

        {/* Location + Rating row */}
        <div className="flex flex-wrap gap-x-4 gap-y-1.5">
          {/* Location */}
          <span className="inline-flex items-center gap-1.5 text-sm text-navy-600">
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
            {crew.city}, {VOIVODESHIP_LABELS[crew.voivodeship]}
          </span>

          {/* Rating */}
          {crew.reviewCount === 0 ? (
            <span className="text-sm text-muted">Brak ocen</span>
          ) : (
            <span
              className="inline-flex items-center gap-1.5"
              aria-label={`Ocena ${crew.avgRating.toFixed(1)} na podstawie ${crew.reviewCount} ${pluralOpinii(crew.reviewCount)}`}
            >
              <svg
                viewBox="0 0 16 16"
                fill="currentColor"
                className="h-3.5 w-3.5 shrink-0 text-brand-500"
                aria-hidden="true"
              >
                <path d="M8 .25a.75.75 0 01.673.418l1.882 3.815 4.21.612a.75.75 0 01.416 1.279l-3.046 2.97.719 4.192a.75.75 0 01-1.088.791L8 12.347l-3.766 1.98a.75.75 0 01-1.088-.79l.72-4.194L.818 6.374a.75.75 0 01.416-1.28l4.21-.611L7.327.668A.75.75 0 018 .25z" />
              </svg>
              <span className="text-sm font-semibold text-navy-800">
                {crew.avgRating.toFixed(1)}
              </span>
              <span className="text-sm text-muted">({crew.reviewCount})</span>
            </span>
          )}
        </div>

        {/* Categories */}
        {crew.serviceCategories.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {visibleCategories.map((cat) => (
              <span
                key={cat.id}
                className="inline-block rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium leading-5 text-brand-700"
              >
                {cat.name}
              </span>
            ))}
            {overflowCount > 0 && (
              <span className="inline-block rounded-full bg-navy-50 px-2.5 py-0.5 text-xs leading-5 text-muted">
                +{overflowCount}
              </span>
            )}
          </div>
        )}

        {/* CTA */}
        <div className="mt-1 flex items-center border-t border-navy-100 pt-3">
          <span className="flex items-center gap-1 text-sm font-medium text-brand-500 transition-colors group-hover:text-brand-600">
            Zobacz profil
            <svg
              viewBox="0 0 16 16"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.75"
              className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5"
              aria-hidden="true"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M3 8h10M9.5 4l4 4-4 4" />
            </svg>
          </span>
        </div>
      </div>
    </Link>
  )
}
