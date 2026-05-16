import type { PhotoModerationItem } from '../../types/photo.types'

interface ModerationCardProps {
  photo: PhotoModerationItem
  onApprove: (id: string) => void
  onReject: (id: string) => void
  onPhotoClick: () => void
  isSubmitting: boolean
  showActions: boolean
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('pl-PL', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

export default function ModerationCard({
  photo,
  onApprove,
  onReject,
  onPhotoClick,
  isSubmitting,
  showActions,
}: ModerationCardProps) {
  const thumb = photo.thumbnailUrl ?? photo.originalUrl

  return (
    <div className="flex flex-col overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm">
      {/* Thumbnail */}
      <button
        type="button"
        className="group relative aspect-[4/3] w-full overflow-hidden bg-navy-50"
        onClick={onPhotoClick}
        aria-label="Podgląd zdjęcia w pełnym rozmiarze"
      >
        <img
          src={thumb}
          alt={photo.caption ?? 'Zdjęcie portfolio'}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          loading="lazy"
        />
        <div className="absolute inset-0 flex items-center justify-center bg-navy-900/0 transition-colors duration-200 group-hover:bg-navy-900/30">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-white/90 shadow-md opacity-0 transition-opacity duration-200 group-hover:opacity-100">
            <ZoomIcon />
          </div>
        </div>
        {!showActions && (
          <div className="absolute left-3 top-3">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-red-500 px-2.5 py-1 text-xs font-semibold text-white shadow-sm">
              <XCircleIcon />
              Odrzucone
            </span>
          </div>
        )}
      </button>

      {/* Meta */}
      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="flex-1">
          {photo.caption && (
            <p className="mb-1.5 line-clamp-2 text-sm font-medium text-navy-800">
              {photo.caption}
            </p>
          )}
          <a
            href={`/ekipy/${photo.crewSlug}`}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs font-semibold text-brand-500 transition-colors hover:text-brand-600"
          >
            {photo.crewCompanyName}
          </a>
          <p className="mt-0.5 text-xs text-muted">{formatDate(photo.uploadedAt)}</p>
        </div>

        {showActions && (
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => onApprove(photo.id)}
              disabled={isSubmitting}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-white transition-colors hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <CheckIcon />
              Zatwierdź
            </button>
            <button
              type="button"
              onClick={() => onReject(photo.id)}
              disabled={isSubmitting}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm font-medium text-red-600 transition-colors hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <XIcon />
              Odrzuć
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function ZoomIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="11" cy="11" r="8" />
      <line x1="21" y1="21" x2="16.65" y2="16.65" />
      <line x1="11" y1="8" x2="11" y2="14" />
      <line x1="8" y1="11" x2="14" y2="11" />
    </svg>
  )
}

function CheckIcon() {
  return (
    <svg
      width="15"
      height="15"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <polyline points="20 6 9 17 4 12" />
    </svg>
  )
}

function XIcon() {
  return (
    <svg
      width="15"
      height="15"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}

function XCircleIcon() {
  return (
    <svg
      width="12"
      height="12"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="10" />
      <line x1="15" y1="9" x2="9" y2="15" />
      <line x1="9" y1="9" x2="15" y2="15" />
    </svg>
  )
}
