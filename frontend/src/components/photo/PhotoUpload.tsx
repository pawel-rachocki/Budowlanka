import { useCallback, useEffect, useRef, useState } from 'react'
import { useUploadPhoto } from '../../hooks/usePhotos'

interface PhotoUploadProps {
  currentCount: number
  onUploaded?: () => void
  disabled?: boolean
}

interface FilePreview {
  file: File
  objectUrl: string
  caption: string
}

const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_SIZE_BYTES = 5 * 1024 * 1024
const MAX_PHOTOS = 20

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export default function PhotoUpload({ currentCount, onUploaded, disabled }: PhotoUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const { uploadPhoto, isUploading } = useUploadPhoto()

  const [dragOver, setDragOver] = useState(false)
  const [preview, setPreview] = useState<FilePreview | null>(null)
  const [progress, setProgress] = useState(0)
  const [validationError, setValidationError] = useState<string | null>(null)

  const isDisabled = currentCount >= MAX_PHOTOS || !!disabled

  useEffect(() => {
    const url = preview?.objectUrl
    return () => {
      if (url) URL.revokeObjectURL(url)
    }
  }, [preview?.objectUrl])

  const validateAndPreview = useCallback((file: File) => {
    setValidationError(null)
    if (!ACCEPTED_TYPES.includes(file.type)) {
      setValidationError('Dozwolone formaty: JPEG, PNG, WebP')
      return
    }
    if (file.size > MAX_SIZE_BYTES) {
      setValidationError(`Plik jest za duży (${formatBytes(file.size)}). Limit: 5 MB`)
      return
    }
    setPreview({ file, objectUrl: URL.createObjectURL(file), caption: '' })
  }, [])

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    if (!isDisabled && !preview) setDragOver(true)
  }
  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    if (e.currentTarget.contains(e.relatedTarget as Node)) return
    setDragOver(false)
  }
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setDragOver(false)
    if (isDisabled || preview) return
    const file = e.dataTransfer.files[0]
    if (file) validateAndPreview(file)
  }
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) validateAndPreview(file)
    e.target.value = ''
  }
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if ((e.key === 'Enter' || e.key === ' ') && !isDisabled && !preview) {
      e.preventDefault()
      inputRef.current?.click()
    }
  }

  const handleUpload = async () => {
    if (!preview) return
    setProgress(0)
    try {
      await uploadPhoto({
        file: preview.file,
        caption: preview.caption || undefined,
        onProgress: (pct) => setProgress(pct < 0 ? -1 : pct),
      })
      setPreview(null)
      setProgress(0)
      onUploaded?.()
    } catch {
      // Toast i reset błędu obsługuje hook
    }
  }

  const handleCancel = () => {
    setPreview(null)
    setProgress(0)
    setValidationError(null)
  }

  return (
    <div className="flex flex-col gap-3">
      {/* Header */}
      <div className="flex items-baseline justify-between border-b border-navy-100 pb-3">
        <div>
          <h3 className="text-sm font-semibold text-navy-900">Zdjęcia portfolio</h3>
          <p className="mt-0.5 text-xs text-muted">Zdjęcia realizacji widoczne na profilu po moderacji</p>
        </div>
        <span
          className={[
            'shrink-0 text-xs font-semibold tabular-nums',
            currentCount >= MAX_PHOTOS ? 'text-red-500' : 'text-navy-600',
          ].join(' ')}
        >
          {currentCount}&thinsp;/&thinsp;{MAX_PHOTOS}
        </span>
      </div>

      {/* Drop Zone */}
      {!preview ? (
        <>
          <div
            role="button"
            tabIndex={isDisabled ? -1 : 0}
            aria-disabled={isDisabled}
            aria-label="Strefa przesyłania zdjęć — kliknij lub przeciągnij plik"
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => !isDisabled && inputRef.current?.click()}
            onKeyDown={handleKeyDown}
            className={[
              'relative flex min-h-44 cursor-pointer flex-col items-center justify-center gap-4 rounded-xl border-2 border-dashed px-6 py-10 text-center outline-none transition-all duration-150',
              'focus-visible:ring-2 focus-visible:ring-brand-500/40 focus-visible:ring-offset-2',
              isDisabled
                ? 'cursor-not-allowed border-navy-100 bg-navy-50/60 opacity-60'
                : dragOver
                  ? 'border-brand-400 bg-brand-50'
                  : 'border-navy-100 bg-surface hover:border-navy-200',
            ].join(' ')}
            style={
              !isDisabled && !dragOver
                ? {
                    backgroundImage: 'radial-gradient(circle, var(--color-navy-200) 1px, transparent 1px)',
                    backgroundSize: '22px 22px',
                  }
                : undefined
            }
          >
            <div
              className={[
                'flex h-14 w-14 items-center justify-center rounded-full border-2 transition-all duration-150',
                dragOver
                  ? 'border-brand-400 bg-brand-100 text-brand-600'
                  : 'border-navy-100 bg-surface-card text-navy-400',
              ].join(' ')}
            >
              <UploadCloudIcon />
            </div>

            <div className="space-y-1.5">
              <p className="text-sm font-semibold text-navy-800">
                {isDisabled
                  ? 'Osiągnięto limit 20 zdjęć'
                  : dragOver
                    ? 'Upuść zdjęcie tutaj'
                    : 'Przeciągnij zdjęcie lub kliknij'}
              </p>
              {!isDisabled && (
                <p className="text-xs text-muted">JPEG · PNG · WebP · max&nbsp;5&nbsp;MB</p>
              )}
            </div>

            <input
              ref={inputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="sr-only"
              aria-label="Wybierz zdjęcie do portfolio"
              disabled={isDisabled}
              onChange={handleFileChange}
            />
          </div>

          {validationError && (
            <p role="alert" aria-live="polite" className="flex items-center gap-1.5 text-xs text-red-500">
              <AlertTriangleIcon />
              {validationError}
            </p>
          )}
        </>
      ) : (
        /* Preview Panel */
        <div className="overflow-hidden rounded-xl border border-navy-100 bg-surface-card shadow-sm">
          {/* Image preview */}
          <div className="relative h-52 w-full overflow-hidden bg-navy-50 sm:h-64">
            <img
              src={preview.objectUrl}
              alt="Podgląd wybranego zdjęcia"
              className="h-full w-full object-cover"
            />
            <span className="absolute right-3 top-3 rounded-full bg-black/50 px-2.5 py-0.5 text-xs font-medium tracking-wide text-white backdrop-blur-sm">
              podgląd
            </span>
          </div>

          {/* Controls */}
          <div className="flex flex-col gap-3 p-4">
            {/* File meta */}
            <div className="flex items-center gap-2">
              <ImageFileIcon />
              <span className="flex-1 truncate text-sm font-medium text-navy-800">
                {preview.file.name}
              </span>
              <span className="shrink-0 text-xs text-muted">{formatBytes(preview.file.size)}</span>
            </div>

            {/* Caption */}
            <input
              type="text"
              placeholder="Opis zdjęcia (opcjonalnie)"
              maxLength={255}
              value={preview.caption}
              disabled={isUploading}
              onChange={(e) => setPreview((p) => (p ? { ...p, caption: e.target.value } : null))}
              className="w-full rounded-lg border border-navy-100 bg-surface px-3 py-2 text-sm text-navy-900 outline-none transition-colors placeholder:text-muted focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 disabled:opacity-60"
            />

            {/* Progress bar */}
            {isUploading && (
              <div>
                <div className="mb-1 flex items-center justify-between text-xs text-muted">
                  <span>Przesyłanie…</span>
                  {progress >= 0 && (
                    <span className="tabular-nums font-medium text-navy-600">{progress}%</span>
                  )}
                </div>
                <div
                  role="progressbar"
                  aria-label="Postęp przesyłania zdjęcia"
                  aria-valuenow={progress >= 0 ? progress : undefined}
                  aria-valuemin={0}
                  aria-valuemax={100}
                  className="h-1.5 w-full overflow-hidden rounded-full bg-navy-100"
                >
                  {progress < 0 ? (
                    <div className="h-full w-2/5 animate-pulse rounded-full bg-brand-500" />
                  ) : (
                    <div
                      className="h-full rounded-full bg-brand-500 transition-all duration-300 ease-out"
                      style={{ width: `${progress}%` }}
                    />
                  )}
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-2 pt-0.5">
              <button
                type="button"
                onClick={() => void handleUpload()}
                disabled={isUploading}
                className="flex-1 rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isUploading ? 'Wysyłanie…' : 'Wyślij zdjęcie'}
              </button>
              <button
                type="button"
                onClick={handleCancel}
                disabled={isUploading}
                className="rounded-lg border border-navy-100 bg-surface-card px-4 py-2.5 text-sm font-medium text-navy-700 transition-colors hover:bg-surface disabled:cursor-not-allowed disabled:opacity-60"
              >
                Anuluj
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function UploadCloudIcon() {
  return (
    <svg
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <polyline points="16 16 12 12 8 16" />
      <line x1="12" y1="12" x2="12" y2="21" />
      <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3" />
    </svg>
  )
}

function ImageFileIcon() {
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
      className="shrink-0 text-navy-400"
    >
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
      <circle cx="8.5" cy="8.5" r="1.5" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  )
}

function AlertTriangleIcon() {
  return (
    <svg
      width="12"
      height="12"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className="shrink-0"
    >
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  )
}
