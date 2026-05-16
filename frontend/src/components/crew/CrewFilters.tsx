import { useMemo } from 'react'
import FormField from '../FormField'
import Select from '../Select'
import { useCategories } from '../../hooks/useCategories'
import type { Voivodeship } from '../../types/crew.types'
import { VOIVODESHIP_LABELS, VOIVODESHIPS_ORDERED } from '../../utils/voivodeships'

export interface CrewFiltersValue {
  /** Pusty string = bez filtra po mieście. */
  city: string
  /** undefined = wszystkie województwa. */
  voivodeship?: Voivodeship
  /** undefined = wszystkie kategorie. */
  categoryId?: string
}

interface CrewFiltersProps {
  value: CrewFiltersValue
  onChange: (next: CrewFiltersValue) => void
  onReset?: () => void
}

export default function CrewFilters({ value, onChange, onReset }: CrewFiltersProps) {
  const hasActiveFilters =
    value.city !== '' || value.voivodeship !== undefined || value.categoryId !== undefined
  const { categories, isLoading, error: categoriesError } = useCategories()

  const voivodeshipOptions = useMemo(
    () => VOIVODESHIPS_ORDERED.map((v) => ({ value: v, label: VOIVODESHIP_LABELS[v] })),
    []
  )

  const categoryOptions = useMemo(
    () => categories.map((c) => ({ value: c.id, label: c.name })),
    [categories]
  )

  return (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:gap-4">
      <div className="grid flex-1 grid-cols-1 gap-4 sm:grid-cols-3">
        <Select
          label="Województwo"
          value={value.voivodeship}
          onChange={(v) => onChange({ ...value, voivodeship: v as Voivodeship | undefined })}
          options={voivodeshipOptions}
          allOptionLabel="Wszystkie województwa"
          placeholder="Wybierz województwo"
        />
        <FormField
          label="Miasto"
          placeholder="np. Warszawa"
          value={value.city}
          onChange={(e) => onChange({ ...value, city: e.target.value })}
        />
        <Select
          label="Kategoria"
          value={value.categoryId}
          onChange={(categoryId) => onChange({ ...value, categoryId })}
          options={categoryOptions}
          allOptionLabel="Wszystkie kategorie"
          placeholder={
            isLoading ? 'Wczytywanie...' : categoriesError ? 'Błąd ładowania' : 'Wybierz kategorię'
          }
          disabled={isLoading}
        />
      </div>
      {onReset && hasActiveFilters && (
        <button
          type="button"
          onClick={onReset}
          className="flex shrink-0 items-center gap-1.5 self-end pb-[9px] text-sm font-medium text-navy-600 hover:text-navy-900 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 rounded"
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
          Wyczyść filtry
        </button>
      )}
    </div>
  )
}
