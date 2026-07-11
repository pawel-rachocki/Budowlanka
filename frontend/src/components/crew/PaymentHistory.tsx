import { useMyPayments } from '../../hooks/usePayments'
import type { PaymentResponse, PaymentStatus, PaymentType } from '../../types/payment.types'
import { formatDate } from '../../utils/formatDate'
import { formatPrice } from '../../utils/formatPrice'

/**
 * Historia płatności na dashboardzie ekipy (E-06 / F8).
 *
 * Autonomiczny komponent — sam pobiera dane przez useMyPayments i obsługuje własne
 * stany (ładowanie / błąd / brak płatności). Dashboard jedynie go renderuje.
 *
 * Layout mobile-first: poniżej sm — lista kart, od sm w górę — tabela. Ekipy korzystają
 * głównie z telefonów, więc surowa tabela na wąskim ekranie nie wchodzi w grę.
 */
export default function PaymentHistory() {
  const { payments, isLoading, error, isFetching } = useMyPayments()

  return (
    <div className="rounded-xl border border-navy-100 bg-surface-card p-6 shadow-sm sm:p-8">
      <div className="mb-6">
        <h2 className="text-lg font-bold text-navy-900">Historia płatności</h2>
        <p className="mt-1 text-sm text-navy-600">
          Twoje zakupy pakietów i Boostów wraz ze statusem transakcji.
        </p>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState />
      ) : payments.length === 0 ? (
        <EmptyState />
      ) : (
        <PaymentTable payments={payments} isFetching={isFetching} />
      )}
    </div>
  )
}

function PaymentTable({
  payments,
  isFetching,
}: {
  payments: PaymentResponse[]
  isFetching: boolean
}) {
  return (
    <div aria-busy={isFetching}>
      {/* Widok tabelaryczny — od sm w górę */}
      <div className="hidden overflow-x-auto sm:block">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-navy-100 text-xs font-semibold uppercase tracking-wide text-muted">
              <th scope="col" className="py-3 pr-4 font-semibold">
                Typ
              </th>
              <th scope="col" className="py-3 pr-4 font-semibold">
                Kwota
              </th>
              <th scope="col" className="py-3 pr-4 font-semibold">
                Status
              </th>
              <th scope="col" className="py-3 font-semibold">
                Data
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-100">
            {payments.map((payment) => (
              <tr key={payment.id}>
                <td className="py-3 pr-4 font-medium text-navy-800">
                  {paymentTypeLabel(payment.paymentType)}
                </td>
                <td className="py-3 pr-4 tabular-nums text-navy-800">
                  {formatPrice(payment.amountPln)}
                </td>
                <td className="py-3 pr-4">
                  <StatusBadge status={payment.status} />
                </td>
                <td className="py-3 text-navy-600">{formatDate(payment.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Widok kart — poniżej sm */}
      <ul className="space-y-3 sm:hidden">
        {payments.map((payment) => (
          <li key={payment.id} className="rounded-lg border border-navy-100 bg-surface px-4 py-3">
            <div className="flex items-center justify-between gap-3">
              <span className="font-semibold text-navy-800">
                {paymentTypeLabel(payment.paymentType)}
              </span>
              <StatusBadge status={payment.status} />
            </div>
            <div className="mt-1 flex items-center justify-between gap-3 text-sm">
              <span className="tabular-nums text-navy-800">{formatPrice(payment.amountPln)}</span>
              <span className="text-navy-600">{formatDate(payment.createdAt)}</span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

function paymentTypeLabel(type: PaymentType): string {
  return type === 'BOOST' ? 'Boost' : 'Pakiet'
}

// Etykiety i kolory statusów — spójne z semantyką badge'y w reszcie panelu.
const STATUS_META: Record<PaymentStatus, { label: string; className: string }> = {
  COMPLETED: { label: 'Opłacona', className: 'bg-green-100 text-green-700' },
  PENDING: { label: 'Oczekuje', className: 'bg-amber-100 text-amber-700' },
  FAILED: { label: 'Nieudana', className: 'bg-red-100 text-red-700' },
  REFUNDED: { label: 'Zwrócona', className: 'bg-navy-100 text-navy-700' },
}

function StatusBadge({ status }: { status: PaymentStatus }) {
  const { label, className } = STATUS_META[status]
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${className}`}
    >
      {label}
    </span>
  )
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center gap-2 rounded-lg bg-surface px-6 py-10 text-center">
      <span
        className="flex h-10 w-10 items-center justify-center rounded-full bg-navy-100 text-navy-500"
        aria-hidden
      >
        <ReceiptIcon />
      </span>
      <p className="text-sm font-semibold text-navy-800">Brak płatności</p>
      <p className="max-w-sm text-sm text-navy-600">
        Gdy wykupisz pakiet lub Boost, historia transakcji pojawi się tutaj.
      </p>
    </div>
  )
}

function LoadingState() {
  return (
    <div className="flex items-center justify-center py-10">
      <div
        role="status"
        aria-label="Ładowanie historii płatności..."
        className="h-8 w-8 animate-spin rounded-full border-4 border-navy-100 border-t-brand-500"
      />
    </div>
  )
}

function ErrorState() {
  return (
    <p className="rounded-lg bg-surface px-4 py-6 text-center text-sm text-navy-600">
      Nie udało się załadować historii płatności. Odśwież stronę.
    </p>
  )
}

function ReceiptIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z" />
      <path d="M8 7h8" />
      <path d="M8 11h8" />
      <path d="M8 15h5" />
    </svg>
  )
}
