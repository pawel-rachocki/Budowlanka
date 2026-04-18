interface PaginationProps {
  /** 0-indexed (Spring Page convention) */
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
}

/** Returns page numbers (1-indexed for display) and ellipsis markers to show. */
function buildPageWindow(current: number, total: number): (number | '...')[] {
  // current is 0-indexed; display as 1-indexed
  const cur = current + 1

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  const pages: (number | '...')[] = []

  if (cur <= 4) {
    // Near the start: 1 2 3 4 5 ... N
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...')
    pages.push(total)
  } else if (cur >= total - 3) {
    // Near the end: 1 ... N-4 N-3 N-2 N-1 N
    pages.push(1)
    pages.push('...')
    for (let i = total - 4; i <= total; i++) pages.push(i)
  } else {
    // Middle: 1 ... cur-1 cur cur+1 ... N
    pages.push(1)
    pages.push('...')
    pages.push(cur - 1)
    pages.push(cur)
    pages.push(cur + 1)
    pages.push('...')
    pages.push(total)
  }

  return pages
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  disabled = false,
}: PaginationProps) {
  if (totalPages <= 1) return null

  const isFirst = currentPage === 0
  const isLast = currentPage === totalPages - 1
  const pageWindow = buildPageWindow(currentPage, totalPages)

  const btnBase =
    'inline-flex items-center justify-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-1'
  const btnNav = `${btnBase} border border-navy-100 text-navy-700 hover:bg-navy-50 hover:border-navy-200`
  const btnPage = `${btnBase} border border-transparent text-navy-700 hover:bg-navy-50 hover:border-navy-100`
  const btnActive = `${btnBase} bg-brand-500 text-white font-semibold border border-brand-500 hover:bg-brand-600`
  const btnDisabled = 'opacity-40 cursor-not-allowed'

  return (
    <nav aria-label="Paginacja wyników" className="mt-8">
      {/* Desktop: full page window */}
      <div className="hidden sm:flex items-center justify-center gap-1">
        {/* Previous */}
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={isFirst || disabled}
          aria-label="Poprzednia strona"
          className={`${btnNav} ${isFirst || disabled ? btnDisabled : ''}`}
        >
          <ChevronLeftIcon />
          Poprzednia
        </button>

        {/* Page numbers */}
        <div className="flex items-center gap-1 mx-1">
          {pageWindow.map((item, idx) =>
            item === '...' ? (
              <span
                key={`ellipsis-${idx}`}
                className="inline-flex items-center justify-center w-9 py-2 text-sm text-muted select-none"
                aria-hidden="true"
              >
                &hellip;
              </span>
            ) : (
              <button
                key={item}
                onClick={() => onPageChange((item as number) - 1)}
                disabled={disabled}
                aria-label={`Strona ${item}`}
                aria-current={item === currentPage + 1 ? 'page' : undefined}
                className={`w-9 ${item === currentPage + 1 ? btnActive : btnPage} ${disabled ? btnDisabled : ''}`}
              >
                {item}
              </button>
            )
          )}
        </div>

        {/* Next */}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={isLast || disabled}
          aria-label="Następna strona"
          className={`${btnNav} ${isLast || disabled ? btnDisabled : ''}`}
        >
          Następna
          <ChevronRightIcon />
        </button>
      </div>

      {/* Mobile: prev / page indicator / next */}
      <div className="flex sm:hidden items-center justify-between gap-2">
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={isFirst || disabled}
          aria-label="Poprzednia strona"
          className={`${btnNav} ${isFirst || disabled ? btnDisabled : ''}`}
        >
          <ChevronLeftIcon />
          Poprzednia
        </button>

        <span className="text-sm text-navy-600 font-medium">
          {currentPage + 1} / {totalPages}
        </span>

        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={isLast || disabled}
          aria-label="Następna strona"
          className={`${btnNav} ${isLast || disabled ? btnDisabled : ''}`}
        >
          Następna
          <ChevronRightIcon />
        </button>
      </div>
    </nav>
  )
}

function ChevronLeftIcon() {
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
      <polyline points="15 18 9 12 15 6" />
    </svg>
  )
}

function ChevronRightIcon() {
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
      <polyline points="9 18 15 12 9 6" />
    </svg>
  )
}
