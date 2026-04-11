import type { ReactNode } from 'react'
import * as RadixSelect from '@radix-ui/react-select'

/**
 * Sentinel value representing the "wszystkie / brak filtra" option.
 * Radix Select forbids empty string as Item value, so we map undefined ↔ sentinel.
 */
const ALL_SENTINEL = '__ALL__'

export interface SelectOption {
  /** Must not be empty string (Radix Select limitation). */
  value: string
  label: string
}

interface SelectProps {
  label: string
  value: string | undefined
  onChange: (value: string | undefined) => void
  options: SelectOption[]
  placeholder?: string
  /**
   * If provided, a leading "Wszystkie..." item is rendered that emits `undefined`
   * when selected (used for resetting a filter).
   */
  allOptionLabel?: string
  disabled?: boolean
  ariaLabel?: string
}

export default function Select({
  label,
  value,
  onChange,
  options,
  placeholder,
  allOptionLabel,
  disabled,
  ariaLabel,
}: SelectProps) {
  // When allOptionLabel is omitted and value is undefined, ALL_SENTINEL matches no item
  // and Radix falls back to showing the placeholder — intentional (uncontrolled empty state).
  const selectValue = value ?? ALL_SENTINEL

  const handleValueChange = (next: string) => {
    onChange(next === ALL_SENTINEL ? undefined : next)
  }

  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium text-navy-800">{label}</label>
      <RadixSelect.Root value={selectValue} onValueChange={handleValueChange} disabled={disabled}>
        <RadixSelect.Trigger
          aria-label={ariaLabel ?? label}
          className={[
            'flex items-center justify-between gap-2 rounded-lg border border-navy-100 bg-surface-card px-3 py-2 text-sm text-navy-900 outline-none transition-colors',
            'focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20',
            'data-[placeholder]:text-muted',
            'disabled:cursor-not-allowed disabled:opacity-60',
          ].join(' ')}
        >
          <RadixSelect.Value placeholder={placeholder} />
          <RadixSelect.Icon className="text-navy-600">
            <ChevronDownIcon />
          </RadixSelect.Icon>
        </RadixSelect.Trigger>
        <RadixSelect.Portal>
          <RadixSelect.Content
            position="popper"
            sideOffset={4}
            className="z-50 overflow-hidden rounded-lg border border-navy-100 bg-surface-card shadow-lg"
          >
            <RadixSelect.Viewport className="max-h-[var(--radix-select-content-available-height)] min-w-[var(--radix-select-trigger-width)] p-1">
              {allOptionLabel && <SelectItem value={ALL_SENTINEL}>{allOptionLabel}</SelectItem>}
              {options.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </RadixSelect.Viewport>
          </RadixSelect.Content>
        </RadixSelect.Portal>
      </RadixSelect.Root>
    </div>
  )
}

interface SelectItemProps {
  value: string
  children: ReactNode
}

function SelectItem({ value, children }: SelectItemProps) {
  return (
    <RadixSelect.Item
      value={value}
      className={[
        'relative flex cursor-pointer select-none items-center rounded-md px-3 py-2 text-sm text-navy-900 outline-none',
        'data-[highlighted]:bg-brand-50 data-[highlighted]:text-brand-700',
        'data-[state=checked]:font-medium data-[state=checked]:text-brand-600',
      ].join(' ')}
    >
      <RadixSelect.ItemText>{children}</RadixSelect.ItemText>
    </RadixSelect.Item>
  )
}

function ChevronDownIcon() {
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
      <polyline points="6 9 12 15 18 9" />
    </svg>
  )
}
