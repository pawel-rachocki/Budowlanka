import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useAdminCrews, useBlockCrew } from '../../hooks/useAdmin'
import type { AdminCrewResponse } from '../../types/admin.types'
import Pagination from '../../components/Pagination'
import AdminDialog from '../../components/admin/AdminDialog'

const PAGE_SIZE = 20

// fix #1: dodano max(500)
const blockSchema = z.object({
  reason: z
    .string()
    .min(5, 'Powód musi mieć co najmniej 5 znaków')
    .max(500, 'Maksymalnie 500 znaków'),
})
type BlockForm = z.infer<typeof blockSchema>

type FilterTab = 'ALL' | 'ACTIVE' | 'BLOCKED'

const TABS: { key: FilterTab; label: string }[] = [
  { key: 'ALL', label: 'Wszystkie' },
  { key: 'ACTIVE', label: 'Aktywne' },
  { key: 'BLOCKED', label: 'Zablokowane' },
]

function tabToParam(tab: FilterTab): boolean | undefined {
  if (tab === 'BLOCKED') return true
  if (tab === 'ACTIVE') return false
  return undefined
}

export default function AdminCrewListPage() {
  const [activeFilter, setActiveFilter] = useState<FilterTab>('ALL')
  const [page, setPage] = useState(0)
  const [blockTarget, setBlockTarget] = useState<AdminCrewResponse | null>(null)
  const [unblockTarget, setUnblockTarget] = useState<AdminCrewResponse | null>(null)

  const { crews, totalElements, totalPages, isLoading, isFetching, error } = useAdminCrews({
    page,
    size: PAGE_SIZE,
    blocked: tabToParam(activeFilter),
  })

  const { blockCrew, isSubmitting } = useBlockCrew()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BlockForm>({ resolver: zodResolver(blockSchema) })

  const handleTabChange = (tab: FilterTab) => {
    setActiveFilter(tab)
    setPage(0)
  }

  const handleBlockOpen = (crew: AdminCrewResponse) => {
    setBlockTarget(crew)
    reset()
  }

  const handleBlockClose = () => {
    setBlockTarget(null)
    reset()
  }

  const onBlockSubmit = (data: BlockForm) => {
    if (!blockTarget) return
    blockCrew(
      { id: blockTarget.id, body: { blocked: true, reason: data.reason } },
      { onSuccess: handleBlockClose },
    )
  }

  const handleUnblockOpen = (crew: AdminCrewResponse) => {
    setUnblockTarget(crew)
  }

  const handleUnblockClose = () => {
    setUnblockTarget(null)
  }

  const handleUnblockConfirm = () => {
    if (!unblockTarget) return
    blockCrew(
      { id: unblockTarget.id, body: { blocked: false } },
      { onSuccess: handleUnblockClose },
    )
  }

  return (
    <div className="p-6 lg:p-8">
      {/* Header */}
      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-navy-900">Ekipy remontowe</h1>
          <p className="mt-1 text-sm text-navy-600">
            Zarządzaj profilami ekip i ich statusem blokady
          </p>
        </div>
        {!isLoading && (
          <span className="inline-flex h-7 shrink-0 items-center rounded-full bg-navy-100 px-3 text-sm font-semibold text-navy-700">
            {totalElements}
          </span>
        )}
      </div>

      {/* Tabs — fix #2: role="tablist" + role="tab" + aria-selected */}
      <div
        role="tablist"
        aria-label="Filtr ekip"
        className="mb-6 flex w-fit gap-1 rounded-xl border border-navy-100 bg-surface-card p-1"
      >
        {TABS.map(({ key, label }) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={activeFilter === key}
            onClick={() => handleTabChange(key)}
            className={[
              'rounded-lg px-4 py-2 text-sm font-medium transition-all',
              activeFilter === key
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
        <SkeletonTable />
      ) : error ? (
        <ErrorState />
      ) : crews.length === 0 ? (
        <EmptyState filter={activeFilter} />
      ) : (
        <>
          <div className={isFetching ? 'opacity-60 transition-opacity' : ''}>
            <CrewTable
              crews={crews}
              onBlock={handleBlockOpen}
              onUnblock={handleUnblockOpen}
              isSubmitting={isSubmitting}
            />
          </div>
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
            disabled={isFetching}
          />
        </>
      )}

      {/* Block modal — fix #3: używa AdminDialog zamiast powtórzonego boilerplate */}
      <AdminDialog
        open={blockTarget !== null}
        onClose={handleBlockClose}
        title="Zablokuj ekipę"
        description={
          <>
            <span className="font-medium text-navy-800">{blockTarget?.companyName}</span> — podaj
            powód blokady. Profil zniknie z publicznego listingu.
          </>
        }
      >
        <form onSubmit={handleSubmit(onBlockSubmit)} className="mt-4 space-y-4">
          <div>
            <label
              htmlFor="block-reason"
              className="mb-1.5 block text-sm font-medium text-navy-800"
            >
              Powód blokady
            </label>
            {/* fix #1: maxLength={500} */}
            <textarea
              id="block-reason"
              {...register('reason')}
              maxLength={500}
              rows={3}
              placeholder="Opisz powód blokady (np. naruszenie regulaminu, fałszywe informacje...)"
              className="w-full resize-none rounded-lg border border-navy-100 px-3 py-2.5 text-sm text-navy-900 placeholder:text-muted focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            />
            {errors.reason && (
              <p className="mt-1 text-xs text-red-500">{errors.reason.message}</p>
            )}
          </div>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={handleBlockClose}
              className="flex-1 rounded-lg border border-navy-100 px-4 py-2.5 text-sm font-medium text-navy-700 transition-colors hover:bg-navy-50"
            >
              Anuluj
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 rounded-lg bg-red-500 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isSubmitting ? 'Blokuję...' : 'Zablokuj'}
            </button>
          </div>
        </form>
      </AdminDialog>

      {/* Unblock confirm modal — fix #3: używa AdminDialog */}
      <AdminDialog
        open={unblockTarget !== null}
        onClose={handleUnblockClose}
        title="Odblokuj ekipę"
        description={
          <>
            <span className="font-medium text-navy-800">{unblockTarget?.companyName}</span> —
            profil powróci do publicznego listingu.
          </>
        }
      >
        <div className="mt-6 flex gap-3">
          <button
            type="button"
            onClick={handleUnblockClose}
            className="flex-1 rounded-lg border border-navy-100 px-4 py-2.5 text-sm font-medium text-navy-700 transition-colors hover:bg-navy-50"
          >
            Anuluj
          </button>
          <button
            type="button"
            onClick={handleUnblockConfirm}
            disabled={isSubmitting}
            className="flex-1 rounded-lg bg-emerald-500 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? 'Odblokowuję...' : 'Odblokuj'}
          </button>
        </div>
      </AdminDialog>
    </div>
  )
}

// ── Crew Table ─────────────────────────────────────────────────────────────────

interface CrewTableProps {
  crews: AdminCrewResponse[]
  onBlock: (crew: AdminCrewResponse) => void
  onUnblock: (crew: AdminCrewResponse) => void
  isSubmitting: boolean
}

function CrewTable({ crews, onBlock, onUnblock, isSubmitting }: CrewTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm">
      {/* Desktop table */}
      <table className="hidden w-full text-sm lg:table">
        <thead>
          <tr className="border-b border-navy-100 bg-navy-50/50">
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-navy-500">
              Ekipa
            </th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-navy-500">
              Lokalizacja
            </th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-navy-500">
              Email właściciela
            </th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-navy-500">
              Status
            </th>
            <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-navy-500">
              Akcja
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-navy-100">
          {crews.map((crew) => (
            <tr key={crew.id} className="transition-colors hover:bg-navy-50/30">
              <td className="px-4 py-3">
                <Link
                  to={`/ekipy/${crew.slug}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="font-medium text-navy-900 transition-colors hover:text-brand-500"
                >
                  {crew.companyName}
                </Link>
              </td>
              <td className="px-4 py-3 text-navy-600">
                {crew.city}, {crew.voivodeship}
              </td>
              <td className="px-4 py-3 text-navy-600">{crew.ownerEmail}</td>
              <td className="px-4 py-3">
                <StatusBadge visible={crew.visible} blocked={crew.blocked} />
              </td>
              <td className="px-4 py-3 text-right">
                <ActionButton
                  crew={crew}
                  onBlock={onBlock}
                  onUnblock={onUnblock}
                  disabled={isSubmitting}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Mobile cards */}
      <ul className="divide-y divide-navy-100 lg:hidden">
        {crews.map((crew) => (
          <li key={crew.id} className="p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0 flex-1">
                <Link
                  to={`/ekipy/${crew.slug}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="block truncate font-semibold text-navy-900 transition-colors hover:text-brand-500"
                >
                  {crew.companyName}
                </Link>
                <p className="mt-0.5 text-sm text-navy-600">
                  {crew.city}, {crew.voivodeship}
                </p>
                <p className="mt-0.5 truncate text-xs text-muted">{crew.ownerEmail}</p>
              </div>
              <div className="flex shrink-0 flex-col items-end gap-2">
                <StatusBadge visible={crew.visible} blocked={crew.blocked} />
                <ActionButton
                  crew={crew}
                  onBlock={onBlock}
                  onUnblock={onUnblock}
                  disabled={isSubmitting}
                />
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

// ── Status Badge ──────────────────────────────────────────────────────────────

function StatusBadge({ visible, blocked }: { visible: boolean; blocked: boolean }) {
  if (blocked) {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-0.5 text-xs font-semibold text-red-600">
        <span className="h-1.5 w-1.5 rounded-full bg-red-500" aria-hidden="true" />
        Zablokowany
      </span>
    )
  }
  if (visible) {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-600">
        <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" aria-hidden="true" />
        Widoczny
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-navy-100 px-2.5 py-0.5 text-xs font-semibold text-navy-600">
      <span className="h-1.5 w-1.5 rounded-full bg-navy-400" aria-hidden="true" />
      Ukryty
    </span>
  )
}

// ── Action Button ─────────────────────────────────────────────────────────────

function ActionButton({
  crew,
  onBlock,
  onUnblock,
  disabled,
}: {
  crew: AdminCrewResponse
  onBlock: (crew: AdminCrewResponse) => void
  onUnblock: (crew: AdminCrewResponse) => void
  disabled: boolean
}) {
  if (crew.blocked) {
    return (
      <button
        type="button"
        onClick={() => onUnblock(crew)}
        disabled={disabled}
        className="rounded-lg border border-emerald-200 px-3 py-1.5 text-xs font-semibold text-emerald-700 transition-colors hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Odblokuj
      </button>
    )
  }
  return (
    <button
      type="button"
      onClick={() => onBlock(crew)}
      disabled={disabled}
      className="rounded-lg border border-red-200 px-3 py-1.5 text-xs font-semibold text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
    >
      Zablokuj
    </button>
  )
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

function SkeletonRow() {
  return (
    <div className="flex items-center gap-6 border-b border-navy-100 px-4 py-3.5 last:border-0">
      <div className="h-4 w-36 animate-pulse rounded bg-navy-100" />
      <div className="h-4 w-28 animate-pulse rounded bg-navy-100" />
      <div className="h-4 w-44 animate-pulse rounded bg-navy-100" />
      <div className="h-5 w-24 animate-pulse rounded-full bg-navy-100" />
      <div className="ml-auto h-7 w-20 animate-pulse rounded-lg bg-navy-100" />
    </div>
  )
}

function SkeletonCard() {
  return (
    <li className="p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 space-y-2">
          <div className="h-4 w-40 animate-pulse rounded bg-navy-100" />
          <div className="h-3 w-28 animate-pulse rounded bg-navy-100" />
          <div className="h-3 w-48 animate-pulse rounded bg-navy-100" />
        </div>
        <div className="flex shrink-0 flex-col items-end gap-2">
          <div className="h-5 w-20 animate-pulse rounded-full bg-navy-100" />
          <div className="h-7 w-20 animate-pulse rounded-lg bg-navy-100" />
        </div>
      </div>
    </li>
  )
}

// fix #4: role="status" + aria-label dla screen readerów
function SkeletonTable() {
  return (
    <div
      role="status"
      aria-label="Ładowanie listy ekip"
      className="overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm"
    >
      <div className="hidden lg:block">
        <div className="border-b border-navy-100 bg-navy-50/50 px-4 py-3">
          <div className="flex gap-12">
            {[144, 112, 160, 96, 80].map((w, i) => (
              <div key={i} className="h-3 animate-pulse rounded bg-navy-100" style={{ width: w }} />
            ))}
          </div>
        </div>
        {Array.from({ length: 5 }).map((_, i) => (
          <SkeletonRow key={i} />
        ))}
      </div>
      <ul className="divide-y divide-navy-100 lg:hidden">
        {Array.from({ length: 5 }).map((_, i) => (
          <SkeletonCard key={i} />
        ))}
      </ul>
    </div>
  )
}

// ── Empty / Error states ──────────────────────────────────────────────────────

function EmptyState({ filter }: { filter: FilterTab }) {
  const copy =
    filter === 'BLOCKED'
      ? { heading: 'Brak zablokowanych ekip', sub: 'Żadna ekipa nie jest aktualnie zablokowana.' }
      : filter === 'ACTIVE'
        ? { heading: 'Brak aktywnych ekip', sub: 'Nie ma ekip spełniających kryteria.' }
        : { heading: 'Brak ekip', sub: 'W systemie nie ma jeszcze żadnych ekip.' }

  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-navy-200 py-16 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-navy-100">
        <UsersIcon />
      </div>
      <h3 className="text-base font-semibold text-navy-800">{copy.heading}</h3>
      <p className="mt-1 max-w-xs text-sm text-navy-600">{copy.sub}</p>
    </div>
  )
}

function ErrorState() {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-red-200 py-16 text-center">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-red-50">
        <AlertIcon />
      </div>
      <h3 className="text-base font-semibold text-red-600">Nie udało się załadować ekip</h3>
      <p className="mt-1 max-w-xs text-sm text-navy-600">
        Odśwież stronę lub spróbuj ponownie za chwilę.
      </p>
    </div>
  )
}

// ── Icons ─────────────────────────────────────────────────────────────────────

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

function UsersIcon() {
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
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  )
}
