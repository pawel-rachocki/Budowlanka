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
}

export default function CrewFilters({ value, onChange }: CrewFiltersProps) {
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
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
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
  )
}
