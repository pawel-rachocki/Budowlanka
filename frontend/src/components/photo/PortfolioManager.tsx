import { useState } from 'react'
import * as Dialog from '@radix-ui/react-dialog'
import { useMyPhotos, useDeletePhoto } from '../../hooks/usePhotos'
import type { PhotoResponse, ModerationStatus } from '../../types/photo.types'
import PhotoUpload from './PhotoUpload'

const MAX_PHOTOS = 20

export default function PortfolioManager() {
  const { photos, isLoading, error } = useMyPhotos()
  const { deletePhoto, isDeleting } = useDeletePhoto()
  const [photoToDelete, setPhotoToDelete] = useState<PhotoResponse | null>(null)

  const handleConfirmDelete = () => {
    if (!photoToDelete) return
    deletePhoto(photoToDelete.id, {
      onSettled: () => setPhotoToDelete(null),
    })
  }

  return (
    <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm">
      <div className="flex items-center justify-between border-b border-navy-100 px-6 py-5">
        <div>
          <h2 className="text-lg font-bold text-navy-900">Portfolio</h2>
          <p className="mt-0.5 text-sm text-navy-600">
            Zdjęcia realizacji widoczne na profilu po akceptacji moderatora
          </p>
        </div>
        <span
          className={[
            'shrink-0 rounded-full px-3 py-1 text-sm font-semibold tabular-nums',
            photos.length >= MAX_PHOTOS ? 'bg-red-100 text-red-600' : 'bg-navy-50 text-navy-600',
          ].join(' ')}
        >
          {photos.length}&thinsp;/&thinsp;{MAX_PHOTOS}
        </span>
      </div>

      <div className="border-b border-navy-100 px-6 py-5">
        <PhotoUpload currentCount={photos.length} disabled={isLoading || photos.length >= MAX_PHOTOS} />
      </div>

      <div className="px-6 py-5">
        {error ? (
          <p className="py-6 text-center text-sm text-red-600">
            Nie udało się załadować zdjęć. Odśwież stronę.
          </p>
        ) : isLoading ? (
          <SkeletonGrid />
        ) : photos.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            {photos.map((photo) => (
              <PhotoCard
                key={photo.id}
                photo={photo}
                onDelete={() => setPhotoToDelete(photo)}
              />
            ))}
          </div>
        )}
      </div>

      <Dialog.Root
        open={!!photoToDelete}
        onOpenChange={(open) => {
          if (!open && !isDeleting) setPhotoToDelete(null)
        }}
      >
        <Dialog.Portal>
          <Dialog.Overlay className="fixed inset-0 z-50 bg-black/50" />
          <Dialog.Content
            className="fixed inset-0 z-50 flex items-center justify-center p-4 outline-none"
          >
            <div className="w-full max-w-sm rounded-2xl bg-surface-card p-6 shadow-xl">
              <Dialog.Title className="text-base font-bold text-navy-900">
                Usuń zdjęcie
              </Dialog.Title>
              <Dialog.Description className="mt-1.5 text-sm text-navy-600">
                Tej operacji nie można cofnąć.
              </Dialog.Description>

              {photoToDelete && (
                <div className="mt-4 flex items-center gap-3">
                  <img
                    src={photoToDelete.thumbnailUrl ?? photoToDelete.url}
                    alt={photoToDelete.caption ?? 'Zdjęcie do usunięcia'}
                    className="h-16 w-16 shrink-0 rounded-lg object-cover"
                  />
                  {photoToDelete.caption && (
                    <p className="line-clamp-2 text-sm text-navy-700">{photoToDelete.caption}</p>
                  )}
                </div>
              )}

              <div className="mt-5 flex gap-2">
                <Dialog.Close asChild>
                  <button
                    type="button"
                    disabled={isDeleting}
                    className="flex-1 rounded-lg border border-navy-100 bg-surface-card px-4 py-2.5 text-sm font-medium text-navy-700 transition-colors hover:bg-surface disabled:opacity-60"
                  >
                    Anuluj
                  </button>
                </Dialog.Close>
                <button
                  type="button"
                  onClick={handleConfirmDelete}
                  disabled={isDeleting}
                  className="flex-1 rounded-lg bg-red-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isDeleting ? 'Usuwanie…' : 'Usuń'}
                </button>
              </div>
            </div>
          </Dialog.Content>
        </Dialog.Portal>
      </Dialog.Root>
    </div>
  )
}

interface PhotoCardProps {
  photo: PhotoResponse
  onDelete: () => void
}

function PhotoCard({ photo, onDelete }: PhotoCardProps) {
  const date = new Date(photo.uploadedAt).toLocaleDateString('pl-PL', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })

  return (
    <div className="overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm">
      <div className="relative aspect-square">
        <img
          src={photo.thumbnailUrl ?? photo.url}
          alt={photo.caption ?? 'Zdjęcie portfolio'}
          className="h-full w-full object-cover"
          loading="lazy"
        />
        <ModerationBadge status={photo.moderationStatus} />
      </div>
      <div className="flex flex-col gap-1.5 p-2.5">
        {photo.caption && (
          <p className="truncate text-xs text-navy-700">{photo.caption}</p>
        )}
        {photo.moderationStatus === 'REJECTED' && photo.moderationNote && (
          <p className="text-xs leading-tight text-red-600">{photo.moderationNote}</p>
        )}
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted">{date}</span>
          <button
            type="button"
            onClick={onDelete}
            aria-label="Usuń zdjęcie"
            className="rounded-md p-1 text-navy-400 transition-colors hover:bg-red-50 hover:text-red-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-300"
          >
            <TrashIcon />
          </button>
        </div>
      </div>
    </div>
  )
}

const MODERATION_BADGE_CONFIG: Record<ModerationStatus, { label: string; className: string }> = {
  PENDING: { label: 'Oczekuje', className: 'bg-amber-100 text-amber-700' },
  APPROVED: { label: 'Zatwierdzone', className: 'bg-green-100 text-green-700' },
  REJECTED: { label: 'Odrzucone', className: 'bg-red-100 text-red-700' },
}

function ModerationBadge({ status }: { status: ModerationStatus }) {
  const { label, className } = MODERATION_BADGE_CONFIG[status]

  return (
    <span
      className={[
        'absolute right-2 top-2 rounded-full px-2 py-0.5 text-[10px] font-semibold leading-none',
        className,
      ].join(' ')}
    >
      {label}
    </span>
  )
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {[0, 1, 2].map((i) => (
        <div key={i} className="animate-pulse overflow-hidden rounded-xl border border-navy-100">
          <div className="aspect-square bg-navy-100" />
          <div className="flex flex-col gap-2 p-2.5">
            <div className="h-3 w-3/4 rounded bg-navy-100" />
            <div className="h-3 w-1/2 rounded bg-navy-100" />
          </div>
        </div>
      ))}
    </div>
  )
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl bg-surface py-12 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-navy-100 text-navy-400">
        <ImagePlaceholderIcon />
      </div>
      <div>
        <p className="text-sm font-semibold text-navy-700">Nie masz jeszcze zdjęć w portfolio</p>
        <p className="mt-0.5 text-xs text-muted">Prześlij pierwsze zdjęcia realizacji</p>
      </div>
    </div>
  )
}

function TrashIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
      <path d="M10 11v6" />
      <path d="M14 11v6" />
      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
    </svg>
  )
}

function ImagePlaceholderIcon() {
  return (
    <svg
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  )
}
