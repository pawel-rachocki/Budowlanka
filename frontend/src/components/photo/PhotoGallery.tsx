import { useState } from 'react'
import type { PublicPhotoResponse } from '../../types/photo.types'
import LightboxModal from './LightboxModal'

interface PhotoGalleryProps {
  photos: PublicPhotoResponse[]
  isLoading?: boolean
}

export default function PhotoGallery({ photos, isLoading }: PhotoGalleryProps) {
  const [openIndex, setOpenIndex] = useState<number | null>(null)

  if (!isLoading && photos.length === 0) return null

  return (
    <div className="rounded-xl border border-navy-100 bg-surface-card shadow-sm p-5">
      <h2 className="text-xs font-semibold uppercase tracking-wider text-muted mb-4">Portfolio</h2>

      {isLoading ? (
        <GallerySkeleton />
      ) : (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
          {photos.map((photo, i) => (
            <Thumbnail key={photo.id} photo={photo} index={i} onOpen={setOpenIndex} />
          ))}
        </div>
      )}

      {openIndex !== null && (
        <LightboxModal
          photos={photos}
          initialIndex={openIndex}
          onClose={() => setOpenIndex(null)}
        />
      )}
    </div>
  )
}

// ── Thumbnail ─────────────────────────────────────────────────────────────────

interface ThumbnailProps {
  photo: PublicPhotoResponse
  index: number
  onOpen: (index: number) => void
}

function Thumbnail({ photo, index, onOpen }: ThumbnailProps) {
  const [loaded, setLoaded] = useState(false)
  const src = photo.thumbnailUrl ?? photo.url

  return (
    <button
      type="button"
      onClick={() => onOpen(index)}
      aria-label={photo.caption ?? `Zdjęcie ${index + 1}, otwórz podgląd`}
      className="group relative aspect-square overflow-hidden rounded-lg bg-navy-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2"
    >
      {/* Blurry placeholder — fades out once image loads */}
      <div
        className={`absolute inset-0 bg-gradient-to-br from-navy-50 to-navy-100 transition-opacity duration-300 ${
          loaded ? 'opacity-0' : 'animate-pulse opacity-100'
        }`}
        aria-hidden="true"
      />

      <img
        src={src}
        alt={photo.caption ?? `Zdjęcie ${index + 1}`}
        loading="lazy"
        draggable={false}
        onLoad={() => setLoaded(true)}
        className={`absolute inset-0 h-full w-full object-cover transition-[opacity,transform] duration-500 ${
          loaded ? 'scale-100 opacity-100' : 'scale-105 opacity-0'
        }`}
      />

      {/* Hover overlay */}
      <div
        className="absolute inset-0 bg-black/0 transition-colors duration-200 group-hover:bg-black/20 group-focus-visible:bg-black/20"
        aria-hidden="true"
      />
    </button>
  )
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

function GallerySkeleton() {
  return (
    <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4">
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="aspect-square animate-pulse rounded-lg bg-navy-100" />
      ))}
    </div>
  )
}
