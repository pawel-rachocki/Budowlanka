import { useState } from 'react'
import { useCrewReviews, useDeleteReview } from '../../hooks/useReviews'
import type { User } from '../../types/auth.types'
import type { ReviewResponse } from '../../types/review.types'
import Pagination from '../Pagination'
import { ReviewCard } from './ReviewCard'
import { ReviewForm } from './ReviewForm'

interface ReviewListProps {
  slug: string
  currentUserEmail: string | null
  currentUserRole: User['role'] | null
}

export function ReviewList({ slug, currentUserEmail, currentUserRole }: ReviewListProps) {
  const [page, setPage] = useState(0)
  const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null)

  const { reviews, totalPages, isLoading, isFetching, error, refetch } = useCrewReviews(slug, page)
  const { deleteReview, isDeleting } = useDeleteReview(slug)

  // authorDisplayName to email użytkownika (per API contract) — porównanie jest celowe.
  // Przy dodaniu authorId do ReviewResponse zamienić na porównanie ID.
  const userOwnReview =
    currentUserEmail !== null
      ? (reviews.find((r) => r.authorDisplayName === currentUserEmail) ?? null)
      : null

  // Sprawdzamy tylko aktualną stronę — edge case (opinia na innej stronie) obsługuje toast 409 w useAddReview.
  const canAddReview =
    currentUserRole === 'CLIENT' && currentUserEmail !== null && userOwnReview === null

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
    setEditingReview(null)
  }

  if (isLoading) {
    return <ReviewListSkeleton />
  }

  if (error) {
    return (
      <div className="rounded-xl border border-navy-100 bg-surface-card p-8 text-center">
        <p className="text-sm text-navy-600">Nie udało się załadować opinii.</p>
        <button
          type="button"
          onClick={() => refetch()}
          className="mt-3 text-sm font-medium text-brand-500 hover:text-brand-600 transition-colors"
        >
          Spróbuj ponownie
        </button>
      </div>
    )
  }

  if (reviews.length === 0) {
    return (
      <div className="flex flex-col gap-3">
        <div className="rounded-xl border border-navy-100 bg-surface-card p-8 text-center">
          <StarPlaceholderIcon />
          <p className="mt-3 text-sm font-medium text-navy-700">Brak opinii — bądź pierwszy!</p>
        </div>
        {canAddReview && (
          <ReviewForm slug={slug} onSuccess={() => setPage(0)} />
        )}
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {canAddReview && !editingReview && (
        <ReviewForm slug={slug} onSuccess={() => setPage(0)} />
      )}
      {reviews.map((review) => {
        const isOwner = currentUserEmail !== null && review.authorDisplayName === currentUserEmail

        if (editingReview?.id === review.id) {
          return (
            <ReviewForm
              key={review.id}
              slug={slug}
              review={review}
              onSuccess={() => setEditingReview(null)}
              onCancel={() => setEditingReview(null)}
            />
          )
        }

        return (
          <ReviewCard
            key={review.id}
            review={review}
            isOwner={isOwner}
            isDeleting={isDeleting}
            onEdit={() => setEditingReview(review)}
            onDelete={() => deleteReview(review.id)}
          />
        )
      })}

      <Pagination
        currentPage={page}
        totalPages={totalPages}
        onPageChange={handlePageChange}
        disabled={isFetching}
      />
    </div>
  )
}

function ReviewListSkeleton() {
  return (
    <div className="flex flex-col gap-3" aria-busy="true" aria-label="Ładowanie opinii">
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          className="animate-pulse rounded-xl border border-navy-100 bg-surface-card shadow-sm p-4"
        >
          <div className="flex items-start gap-3">
            <div className="h-9 w-9 shrink-0 rounded-full bg-navy-100" />
            <div className="flex flex-1 flex-col gap-2">
              <div className="h-3.5 w-32 rounded bg-navy-100" />
              <div className="h-3 w-24 rounded bg-navy-100" />
            </div>
          </div>
          <div className="mt-3 space-y-2">
            <div className="h-3 w-full rounded bg-navy-100" />
            <div className="h-3 w-4/5 rounded bg-navy-100" />
          </div>
        </div>
      ))}
    </div>
  )
}

function StarPlaceholderIcon() {
  return (
    <svg
      className="mx-auto h-10 w-10 text-navy-200"
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
    >
      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
    </svg>
  )
}
