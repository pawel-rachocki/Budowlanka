import { useEffect, useRef, useState } from 'react'
import * as Dialog from '@radix-ui/react-dialog'
import type { PublicPhotoResponse } from '../../types/photo.types'

interface LightboxModalProps {
  photos: PublicPhotoResponse[]
  initialIndex: number
  onClose: () => void
}

export default function LightboxModal({ photos, initialIndex, onClose }: LightboxModalProps) {
  const [index, setIndex] = useState(initialIndex)
  const touchStartX = useRef<number | null>(null)

  const total = photos.length
  const current = photos[index]

  const goPrev = () => setIndex((i) => (i - 1 + total) % total)
  const goNext = () => setIndex((i) => (i + 1) % total)

  useEffect(() => {
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft') setIndex((i) => (i - 1 + total) % total)
      if (e.key === 'ArrowRight') setIndex((i) => (i + 1) % total)
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [total])

  if (total === 0 || !current) return null

  return (
    <Dialog.Root
      open
      onOpenChange={(open) => {
        if (!open) onClose()
      }}
    >
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/85 data-[state=open]:animate-lightbox-in data-[state=closed]:animate-lightbox-out" />

        <Dialog.Content
          aria-describedby={undefined}
          className="fixed inset-0 z-50 flex items-center justify-center outline-none data-[state=open]:animate-lightbox-content-in data-[state=closed]:animate-lightbox-content-out"
          onPointerDown={(e) => {
            if (e.target === e.currentTarget) onClose()
          }}
          onTouchStart={(e) => {
            touchStartX.current = e.touches[0].clientX
          }}
          onTouchEnd={(e) => {
            if (touchStartX.current === null) return
            const delta = touchStartX.current - e.changedTouches[0].clientX
            if (delta > 50) goNext()
            else if (delta < -50) goPrev()
            touchStartX.current = null
          }}
        >
          <Dialog.Title className="sr-only">
            Podgląd zdjęcia {index + 1} z {total}
          </Dialog.Title>

          {/* Counter + close */}
          <div className="absolute top-4 right-4 z-10 flex items-center gap-3">
            <span className="tabular-nums text-sm font-medium text-white/70">
              {index + 1} / {total}
            </span>
            <Dialog.Close className="flex h-9 w-9 items-center justify-center rounded-full bg-white/10 text-white/80 transition-colors hover:bg-white/20 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/50">
              <CloseIcon />
            </Dialog.Close>
          </div>

          {/* Prev */}
          {total > 1 && (
            <button
              type="button"
              onClick={goPrev}
              aria-label="Poprzednie zdjęcie"
              className="absolute left-2 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-white/10 text-white/80 transition-colors hover:bg-white/20 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/50 sm:left-4"
            >
              <ChevronLeftIcon />
            </button>
          )}

          {/* Image + caption */}
          <div className="flex max-w-4xl w-full flex-col items-center gap-3 px-16">
            <img
              key={current.id}
              src={current.url}
              alt={current.caption ?? `Zdjęcie ${index + 1}`}
              className="max-h-[75vh] max-w-full rounded-lg object-contain shadow-2xl animate-photo-change"
              draggable={false}
            />
            {current.caption && (
              <p className="max-w-md text-center text-sm text-white/80">{current.caption}</p>
            )}
          </div>

          {/* Next */}
          {total > 1 && (
            <button
              type="button"
              onClick={goNext}
              aria-label="Następne zdjęcie"
              className="absolute right-2 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-white/10 text-white/80 transition-colors hover:bg-white/20 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/50 sm:right-4"
            >
              <ChevronRightIcon />
            </button>
          )}
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}

// ── Icons ─────────────────────────────────────────────────────────────────────

function CloseIcon() {
  return (
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
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}

function ChevronLeftIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-5 w-5"
      aria-hidden="true"
    >
      <polyline points="15 18 9 12 15 6" />
    </svg>
  )
}

function ChevronRightIcon() {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="h-5 w-5"
      aria-hidden="true"
    >
      <polyline points="9 18 15 12 9 6" />
    </svg>
  )
}
