export default function CrewCardSkeleton() {
  return (
    <div
      className="relative overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm animate-pulse"
      aria-hidden="true"
    >
      {/* Left accent strip */}
      <div className="absolute inset-y-0 left-0 w-1 bg-brand-500/20" />

      <div className="flex flex-col gap-3 py-5 pr-5 pl-6 sm:py-6 sm:pr-6 sm:pl-7">
        {/* Company name */}
        <div className="h-5 w-3/5 rounded bg-navy-100" />

        {/* Location + Rating row */}
        <div className="flex gap-4">
          <div className="h-4 w-28 rounded bg-navy-100" />
          <div className="h-4 w-16 rounded bg-navy-100" />
        </div>

        {/* Category pills */}
        <div className="flex gap-1.5">
          <div className="h-5 w-16 rounded-full bg-brand-500/10" />
          <div className="h-5 w-20 rounded-full bg-brand-500/10" />
          <div className="h-5 w-14 rounded-full bg-brand-500/10" />
        </div>

        {/* CTA */}
        <div className="mt-1 border-t border-navy-100 pt-3">
          <div className="h-4 w-24 rounded bg-navy-100" />
        </div>
      </div>
    </div>
  )
}
