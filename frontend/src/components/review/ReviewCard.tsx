import type { ReviewResponse } from '../../types/review.types'
import { formatDate } from '../../utils/formatDate'
import { StarRating } from './StarRating'

interface ReviewCardProps {
  review: ReviewResponse
  isOwner: boolean
  isDeleting?: boolean
  onEdit: () => void
  onDelete: () => void
}

export function ReviewCard({
  review,
  isOwner,
  isDeleting = false,
  onEdit,
  onDelete,
}: ReviewCardProps) {
  const initials = (review.authorDisplayName.slice(0, 2) || '??').toUpperCase()

  return (
    <article className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-4">
      <div className="flex items-start gap-3">
        <div
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-500 text-sm font-semibold text-white"
          aria-hidden="true"
        >
          {initials}
        </div>

        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <div className="flex items-center justify-between gap-2">
            <div className="flex min-w-0 flex-col gap-0.5">
              <span className="truncate text-sm font-medium text-navy-800">
                {review.authorDisplayName}
              </span>
              <div className="flex items-center gap-2">
                <StarRating rating={review.rating} size="sm" />
                <span className="text-xs text-muted">{formatDate(review.createdAt)}</span>
              </div>
            </div>

            {isOwner && (
              <div className="flex shrink-0 items-center gap-3">
                <button
                  type="button"
                  onClick={onEdit}
                  disabled={isDeleting}
                  aria-label="Edytuj opinię"
                  className="rounded text-xs text-navy-600 transition-colors hover:text-navy-900 disabled:opacity-40 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
                >
                  Edytuj
                </button>
                <button
                  type="button"
                  onClick={onDelete}
                  disabled={isDeleting}
                  aria-label="Usuń opinię"
                  className="rounded text-xs text-red-500 transition-colors hover:text-red-700 disabled:opacity-40 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
                >
                  {isDeleting ? 'Usuwanie…' : 'Usuń'}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {review.comment !== null && (
        <p className="mt-3 text-sm leading-relaxed text-navy-700">{review.comment}</p>
      )}
    </article>
  )
}
