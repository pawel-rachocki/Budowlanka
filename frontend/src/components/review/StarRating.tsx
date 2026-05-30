import { STAR_PATH } from './starConstants'

const SIZE_CLASSES = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
}

interface StarRatingProps {
  rating: number
  size?: 'sm' | 'md'
}

export function StarRating({ rating, size = 'sm' }: StarRatingProps) {
  return (
    <div className="flex gap-0.5" role="img" aria-label={`Ocena: ${rating} na 5`}>
      {Array.from({ length: 5 }).map((_, i) => (
        <svg
          key={i}
          viewBox="0 0 16 16"
          fill="currentColor"
          className={`${SIZE_CLASSES[size]} ${i < rating ? 'text-brand-500' : 'text-navy-100'}`}
          aria-hidden="true"
        >
          <path d={STAR_PATH} />
        </svg>
      ))}
    </div>
  )
}
