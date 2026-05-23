import { useState } from 'react'
import { STAR_PATH } from './starConstants'

interface StarRatingInputProps {
  value: number
  onChange: (rating: number) => void
  disabled?: boolean
}


export function StarRatingInput({ value, onChange, disabled = false }: StarRatingInputProps) {
  const [hoverValue, setHoverValue] = useState(0)

  // Derive effective hover: when disabled, treat as no hover regardless of mouse state
  const activeHover = disabled ? 0 : hoverValue

  const getStarColor = (starIndex: number) => {
    const active = activeHover > 0 ? starIndex <= activeHover : starIndex <= value
    if (!active) return 'text-navy-100'
    return activeHover > 0 ? 'text-brand-400' : 'text-brand-500'
  }

  const getTabIndex = (starIndex: number) => {
    if (disabled) return -1
    // Roving tabindex: only the selected star (or first star if none selected) is in tab sequence
    return value === starIndex || (value === 0 && starIndex === 1) ? 0 : -1
  }

  return (
    <div
      role="radiogroup"
      aria-label="Ocena"
      className="flex gap-1"
      onMouseLeave={() => !disabled && setHoverValue(0)}
    >
      {Array.from({ length: 5 }).map((_, i) => {
        const starIndex = i + 1
        const isChecked = value === starIndex
        const starLabel = starIndex === 1 ? 'gwiazdka' : starIndex < 5 ? 'gwiazdki' : 'gwiazdek'
        const buttonClass = [
          'p-0.5 rounded transition-colors',
          'focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500',
          disabled ? 'cursor-default' : 'cursor-pointer',
          getStarColor(starIndex),
        ].join(' ')

        return (
          <button
            key={starIndex}
            type="button"
            role="radio"
            aria-label={`${starIndex} ${starLabel}`}
            aria-checked={isChecked}
            tabIndex={getTabIndex(starIndex)}
            disabled={disabled}
            className={buttonClass}
            onMouseEnter={() => !disabled && setHoverValue(starIndex)}
            onClick={() => onChange(starIndex)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                onChange(starIndex)
              }
              if (e.key === 'ArrowRight') onChange(Math.min(5, value + 1))
              if (e.key === 'ArrowLeft') onChange(Math.max(1, value - 1))
            }}
          >
            <svg
              viewBox="0 0 16 16"
              fill="currentColor"
              className="h-7 w-7"
              aria-hidden="true"
            >
              <path d={STAR_PATH} />
            </svg>
          </button>
        )
      })}
    </div>
  )
}
