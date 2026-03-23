import { forwardRef, InputHTMLAttributes } from 'react'

interface FormFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
}

const FormField = forwardRef<HTMLInputElement, FormFieldProps>(
  ({ label, error, id, ...props }, ref) => {
    const fieldId =
      id ??
      label
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/\s+/g, '-')
    const errorId = error ? `${fieldId}-error` : undefined

    return (
      <div className="flex flex-col gap-1">
        <label htmlFor={fieldId} className="text-sm font-medium text-navy-800">
          {label}
        </label>
        <input
          ref={ref}
          id={fieldId}
          aria-invalid={error ? true : undefined}
          aria-describedby={errorId}
          className={[
            'rounded-lg border px-3 py-2 text-sm text-navy-900 outline-none transition-colors placeholder:text-muted',
            'focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20',
            error ? 'border-red-400 bg-red-50' : 'border-navy-100 bg-surface-card',
          ].join(' ')}
          {...props}
        />
        {error && (
          <p id={errorId} role="alert" className="text-xs text-red-500">
            {error}
          </p>
        )}
      </div>
    )
  }
)

FormField.displayName = 'FormField'

export default FormField
