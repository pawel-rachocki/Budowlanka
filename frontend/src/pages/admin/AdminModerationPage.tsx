import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import * as Dialog from '@radix-ui/react-dialog'
import { useModerationQueue, useModeratePhoto } from '../../hooks/useAdmin'
import type { PublicPhotoResponse } from '../../types/photo.types'
import ModerationCard from '../../components/admin/ModerationCard'
import LightboxModal from '../../components/photo/LightboxModal'
import Pagination from '../../components/Pagination'

const PAGE_SIZE = 12

const rejectSchema = z.object({
  note: z.string().min(5, 'Notatka musi mieć co najmniej 5 znaków'),
})

type RejectForm = z.infer<typeof rejectSchema>

type ActiveTab = 'PENDING' | 'REJECTED'

const TABS: { key: ActiveTab; label: string }[] = [
  { key: 'PENDING', label: 'Oczekujące' },
  { key: 'REJECTED', label: 'Odrzucone' },
]

export default function AdminModerationPage() {
  const [activeTab, setActiveTab] = useState<ActiveTab>('PENDING')
  const [page, setPage] = useState(0)
  const [lightboxIndex, setLightboxIndex] = useState<number | null>(null)
  const [rejectTarget, setRejectTarget] = useState<string | null>(null)

  const { queue, totalElements, totalPages, isLoading, isFetching, error } =
    useModerationQueue(activeTab, page, PAGE_SIZE)

  const { moderatePhoto, isSubmitting } = useModeratePhoto()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RejectForm>({ resolver: zodResolver(rejectSchema) })

  const handleTabChange = (tab: ActiveTab) => {
    setActiveTab(tab)
    setPage(0)
  }

  const handleApprove = (id: string) => {
    moderatePhoto({ id, body: { decision: 'APPROVE' } }, { onSuccess: () => setLightboxIndex(null) })
  }

  const handleRejectOpen = (id: string) => {
    setRejectTarget(id)
    reset()
  }

  const handleRejectClose = () => {
    setRejectTarget(null)
    reset()
  }

  const onRejectSubmit = (data: RejectForm) => {
    if (!rejectTarget) return
    moderatePhoto(
      { id: rejectTarget, body: { decision: 'REJECT', note: data.note } },
      {
        onSuccess: () => {
          handleRejectClose()
          setLightboxIndex(null)
        },
      },
    )
  }

  const lightboxPhotos = useMemo<PublicPhotoResponse[]>(
    () =>
      queue.map((p) => ({
        id: p.id,
        url: p.originalUrl,
        thumbnailUrl: p.thumbnailUrl,
        caption: p.caption,
        uploadedAt: p.uploadedAt,
      })),
    [queue],
  )

  return (
    <div className="p-6 lg:p-8">
      {/* Header */}
      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-navy-900">Moderacja zdjęć</h1>
          <p className="mt-1 text-sm text-navy-600">
            Przeglądaj i moderuj zdjęcia portfolio ekip remontowych
          </p>
        </div>
        {!isLoading && (
          <span className="inline-flex h-7 shrink-0 items-center rounded-full bg-navy-100 px-3 text-sm font-semibold text-navy-700">
            {totalElements}
          </span>
        )}
      </div>

      {/* Tabs */}
      <div className="mb-6 flex w-fit gap-1 rounded-xl border border-navy-100 bg-surface-card p-1">
        {TABS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            onClick={() => handleTabChange(key)}
            className={[
              'rounded-lg px-4 py-2 text-sm font-medium transition-all',
              activeTab === key
                ? 'bg-navy-900 text-white shadow-sm'
                : 'text-navy-600 hover:text-navy-900',
            ].join(' ')}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Content */}
      {isLoading ? (
        <SkeletonGrid />
      ) : error ? (
        <ErrorState />
      ) : queue.length === 0 ? (
        <EmptyState tab={activeTab} />
      ) : (
        <>
          <div
            className={[
              'grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3',
              isFetching ? 'opacity-60 transition-opacity' : '',
            ].join(' ')}
          >
            {queue.map((photo, index) => (
              <ModerationCard
                key={photo.id}
                photo={photo}
                onApprove={handleApprove}
                onReject={handleRejectOpen}
                onPhotoClick={() => setLightboxIndex(index)}
                isSubmitting={isSubmitting}
                showActions={activeTab === 'PENDING'}
              />
            ))}
          </div>
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
            disabled={isFetching}
          />
        </>
      )}

      {/* Reject modal */}
      <Dialog.Root
        open={rejectTarget !== null}
        onOpenChange={(open) => {
          if (!open) handleRejectClose()
        }}
      >
        <Dialog.Portal>
          <Dialog.Overlay className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
          <Dialog.Content className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-surface-card p-6 shadow-xl focus:outline-none">
            <Dialog.Close className="absolute right-4 top-4 flex h-8 w-8 items-center justify-center rounded-lg text-navy-400 transition-colors hover:bg-navy-50 hover:text-navy-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500">
              <ModalCloseIcon />
              <span className="sr-only">Zamknij</span>
            </Dialog.Close>
            <Dialog.Title className="text-lg font-bold text-navy-900">
              Odrzuć zdjęcie
            </Dialog.Title>
            <Dialog.Description className="mt-1 text-sm text-navy-600">
              Podaj powód odrzucenia. Notatka zostanie zapisana przy zdjęciu.
            </Dialog.Description>

            <form onSubmit={handleSubmit(onRejectSubmit)} className="mt-4 space-y-4">
              <div>
                <label
                  htmlFor="reject-note"
                  className="mb-1.5 block text-sm font-medium text-navy-800"
                >
                  Notatka moderatora
                </label>
                <textarea
                  id="reject-note"
                  {...register('note')}
                  rows={4}
                  placeholder="Opisz powód odrzucenia (np. treść nieodpowiednia, zdjęcie niezwiązane z usługami remontowymi...)"
                  className="w-full resize-none rounded-lg border border-navy-100 px-3 py-2.5 text-sm text-navy-900 placeholder:text-muted focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
                />
                {errors.note && (
                  <p className="mt-1 text-xs text-red-500">{errors.note.message}</p>
                )}
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={handleRejectClose}
                  className="flex-1 rounded-lg border border-navy-100 px-4 py-2.5 text-sm font-medium text-navy-700 transition-colors hover:bg-navy-50"
                >
                  Anuluj
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="flex-1 rounded-lg bg-red-500 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {isSubmitting ? 'Zapisuję...' : 'Odrzuć zdjęcie'}
                </button>
              </div>
            </form>
          </Dialog.Content>
        </Dialog.Portal>
      </Dialog.Root>

      {/* Lightbox */}
      {lightboxIndex !== null && lightboxPhotos.length > 0 && (
        <LightboxModal
          photos={lightboxPhotos}
          initialIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
        />
      )}
    </div>
  )
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

function SkeletonCard() {
  return (
    <div className="overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm">
      <div className="aspect-[4/3] w-full animate-pulse bg-navy-100" />
      <div className="space-y-3 p-4">
        <div className="h-4 w-3/4 animate-pulse rounded bg-navy-100" />
        <div className="h-3 w-1/2 animate-pulse rounded bg-navy-100" />
        <div className="flex gap-2 pt-1">
          <div className="h-9 flex-1 animate-pulse rounded-lg bg-navy-100" />
          <div className="h-9 flex-1 animate-pulse rounded-lg bg-navy-100" />
        </div>
      </div>
    </div>
  )
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: 6 }).map((_, i) => (
        <SkeletonCard key={i} />
      ))}
    </div>
  )
}

// ── Error state ───────────────────────────────────────────────────────────────

function ErrorState() {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-red-200 py-16 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-red-50">
        <AlertIcon />
      </div>
      <h3 className="text-base font-semibold text-red-600">Nie udało się załadować zdjęć</h3>
      <p className="mt-1 max-w-xs text-sm text-navy-600">
        Odśwież stronę lub spróbuj ponownie za chwilę.
      </p>
    </div>
  )
}

// ── Empty state ───────────────────────────────────────────────────────────────

function EmptyState({ tab }: { tab: ActiveTab }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-navy-200 py-16 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-navy-100">
        {tab === 'PENDING' ? <CheckCircleIcon /> : <ArchiveIcon />}
      </div>
      <h3 className="text-base font-semibold text-navy-800">
        {tab === 'PENDING' ? 'Kolejka pusta' : 'Brak odrzuconych zdjęć'}
      </h3>
      <p className="mt-1 max-w-xs text-sm text-navy-600">
        {tab === 'PENDING'
          ? 'Wszystkie zdjęcia zostały zmoderowane. Dobra robota!'
          : 'Żadne zdjęcie nie zostało jeszcze odrzucone.'}
      </p>
    </div>
  )
}

// ── Icons ─────────────────────────────────────────────────────────────────────

function ModalCloseIcon() {
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
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}

function AlertIcon() {
  return (
    <svg
      width="28"
      height="28"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="text-red-500"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  )
}

function CheckCircleIcon() {
  return (
    <svg
      width="28"
      height="28"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="text-emerald-500"
      aria-hidden="true"
    >
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  )
}

function ArchiveIcon() {
  return (
    <svg
      width="28"
      height="28"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="text-navy-400"
      aria-hidden="true"
    >
      <polyline points="21 8 21 21 3 21 3 8" />
      <rect x="1" y="3" width="22" height="5" />
      <line x1="10" y1="12" x2="14" y2="12" />
    </svg>
  )
}
