import { useEffect, useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import axios from 'axios'
import FormField from '../FormField'
import Select from '../Select'
import { useCategories } from '../../hooks/useCategories'
import { useToast } from '../../hooks/useToast'
import { crewApi } from '../../api/crew.api'
import { VOIVODESHIP_LABELS, VOIVODESHIPS_ORDERED } from '../../utils/voivodeships'
import type { ApiError } from '../../types/api.types'
import type { CrewProfileResponse, Voivodeship } from '../../types/crew.types'

const VOIVODESHIPS = Object.keys(VOIVODESHIP_LABELS) as [Voivodeship, ...Voivodeship[]]

const schema = z.object({
  companyName: z.string().min(1, 'Nazwa firmy jest wymagana').max(255, 'Maks. 255 znaków'),
  description: z.string().max(2000, 'Maks. 2000 znaków'),
  phone: z.string().max(20, 'Maks. 20 znaków'),
  contactEmail: z
    .string()
    .refine((v) => !v || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v), 'Nieprawidłowy adres email'),
  city: z.string().min(1, 'Miasto jest wymagane').max(100, 'Maks. 100 znaków'),
  voivodeship: z.enum(VOIVODESHIPS, { error: 'Wybierz województwo' }),
  serviceRadiusKm: z
    .string()
    .refine((v) => !v || (/^\d+$/.test(v) && +v >= 1 && +v <= 500), 'Podaj liczbę od 1 do 500'),
  nip: z.string().refine((v) => !v || /^\d{10}$/.test(v), 'NIP musi składać się z 10 cyfr'),
  categoryIds: z.array(z.string()),
})

type FormData = z.infer<typeof schema>

interface CrewProfileFormProps {
  profile: CrewProfileResponse | null
  onSuccess: () => void
}

const voivodeshipOptions = VOIVODESHIPS_ORDERED.map((v) => ({
  value: v,
  label: VOIVODESHIP_LABELS[v],
}))

function buildDefaultValues(p: CrewProfileResponse | null): FormData {
  return {
    companyName: p?.companyName ?? '',
    description: p?.description ?? '',
    phone: p?.phone ?? '',
    contactEmail: p?.contactEmail ?? '',
    city: p?.city ?? '',
    // Pusty string = stan "nie wybrano"; z.enum odrzuci go przy submit ("Wybierz województwo").
    voivodeship: (p?.voivodeship ?? '') as Voivodeship,
    serviceRadiusKm: p?.serviceRadiusKm?.toString() ?? '',
    nip: p?.nip ?? '',
    categoryIds: p?.serviceCategories?.map((c) => c.id) ?? [],
  }
}

export default function CrewProfileForm({ profile, onSuccess }: CrewProfileFormProps) {
  const { showToast } = useToast()
  const { categories, isLoading: categoriesLoading } = useCategories()
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: buildDefaultValues(profile),
  })

  useEffect(() => {
    reset(buildDefaultValues(profile))
  }, [profile, reset])

  const watchedCategoryIds = useWatch({ control, name: 'categoryIds' }) ?? []

  function toggleCategory(id: string) {
    const current = watchedCategoryIds
    setValue(
      'categoryIds',
      current.includes(id) ? current.filter((c) => c !== id) : [...current, id],
      { shouldDirty: true }
    )
  }

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    const request = {
      companyName: data.companyName,
      description: data.description || undefined,
      phone: data.phone || undefined,
      contactEmail: data.contactEmail || undefined,
      city: data.city,
      voivodeship: data.voivodeship,
      serviceRadiusKm: data.serviceRadiusKm ? Number(data.serviceRadiusKm) : undefined,
      nip: data.nip || undefined,
      categoryIds: data.categoryIds.length ? data.categoryIds : undefined,
    }
    try {
      if (profile) {
        await crewApi.updateProfile(request)
      } else {
        await crewApi.createProfile(request)
      }
      showToast(
        profile ? 'Profil zaktualizowany pomyślnie.' : 'Profil ekipy utworzony pomyślnie!',
        'success'
      )
      onSuccess()
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const apiError = err.response?.data as ApiError | undefined
        if (err.response?.status === 409) {
          setServerError('Profil dla tego konta już istnieje.')
        } else {
          setServerError(
            (apiError?.message ?? 'Nie udało się zapisać profilu. Spróbuj ponownie.').slice(0, 200)
          )
        }
      } else {
        setServerError('Wystąpił nieoczekiwany błąd.')
      }
      showToast('Nie udało się zapisać profilu.', 'error')
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-8">
      <fieldset disabled={isSubmitting} className="contents">
        {/* Informacje podstawowe */}
        <section className="flex flex-col gap-4">
          <SectionHeader
            title="Informacje podstawowe"
            subtitle="Nazwa i opis Twojej firmy widoczne na profilu publicznym"
          />
          <FormField
            label="Nazwa firmy *"
            id="companyName"
            placeholder="np. Kowalski Remonty"
            error={errors.companyName?.message}
            {...register('companyName')}
          />
          <div className="flex flex-col gap-1">
            <label htmlFor="description" className="text-sm font-medium text-navy-800">
              Opis działalności
            </label>
            <textarea
              id="description"
              rows={4}
              placeholder="Opisz swoją firmę, doświadczenie i specjalizacje..."
              aria-invalid={errors.description ? true : undefined}
              aria-describedby={errors.description ? 'description-error' : undefined}
              className={[
                'w-full resize-none rounded-lg border px-3 py-2 text-sm text-navy-900 outline-none transition-colors placeholder:text-muted',
                'focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20',
                errors.description ? 'border-red-400 bg-red-50' : 'border-navy-100 bg-surface-card',
              ].join(' ')}
              {...register('description')}
            />
            {errors.description && (
              <p id="description-error" role="alert" className="text-xs text-red-500">
                {errors.description.message}
              </p>
            )}
          </div>
        </section>

        {/* Kontakt */}
        <section className="flex flex-col gap-4">
          <SectionHeader
            title="Kontakt"
            subtitle="Dane kontaktowe widoczne tylko dla zalogowanych użytkowników"
          />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormField
              label="Telefon"
              id="phone"
              type="tel"
              placeholder="600 100 200"
              error={errors.phone?.message}
              {...register('phone')}
            />
            <FormField
              label="Email kontaktowy"
              id="contactEmail"
              type="email"
              placeholder="kontakt@twojafirma.pl"
              error={errors.contactEmail?.message}
              {...register('contactEmail')}
            />
          </div>
        </section>

        {/* Lokalizacja */}
        <section className="flex flex-col gap-4">
          <SectionHeader title="Lokalizacja" subtitle="Miasto, z którego działasz i zasięg usług" />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormField
              label="Miasto *"
              id="city"
              placeholder="np. Warszawa"
              error={errors.city?.message}
              {...register('city')}
            />
            <Controller
              name="voivodeship"
              control={control}
              render={({ field, fieldState }) => (
                <div className="flex flex-col gap-1">
                  <Select
                    label="Województwo *"
                    value={field.value || undefined}
                    onChange={(v) => field.onChange(v ?? '')}
                    options={voivodeshipOptions}
                    placeholder="Wybierz województwo"
                  />
                  {fieldState.error && (
                    <p role="alert" className="text-xs text-red-500">
                      {fieldState.error.message}
                    </p>
                  )}
                </div>
              )}
            />
          </div>
          <div className="sm:w-1/2 sm:pr-2">
            <FormField
              label="Zasięg usług (km)"
              id="serviceRadiusKm"
              type="number"
              min={1}
              max={500}
              placeholder="np. 50"
              error={errors.serviceRadiusKm?.message}
              {...register('serviceRadiusKm')}
            />
          </div>
        </section>

        {/* Dane firmowe */}
        <section className="flex flex-col gap-4">
          <SectionHeader title="Dane firmowe" subtitle="Opcjonalne dane identyfikacyjne" />
          <div className="sm:w-1/2 sm:pr-2">
            <FormField
              label="NIP"
              id="nip"
              placeholder="10 cyfr bez separatorów"
              error={errors.nip?.message}
              {...register('nip')}
            />
          </div>
        </section>

        {/* Kategorie usług */}
        <section className="flex flex-col gap-4">
          <SectionHeader
            title="Kategorie usług"
            subtitle="Zaznacz wszystkie rodzaje prac, które wykonujesz"
          />
          {categoriesLoading ? (
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <div
                  key={i}
                  className="h-11 animate-pulse rounded-lg border border-navy-100 bg-surface"
                />
              ))}
            </div>
          ) : categories.length === 0 ? (
            <p className="text-sm text-muted">Brak dostępnych kategorii.</p>
          ) : (
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
              {categories.map((cat) => {
                const checked = watchedCategoryIds.includes(cat.id)
                return (
                  <label
                    key={cat.id}
                    className={[
                      'flex cursor-pointer select-none items-center gap-2.5 rounded-lg border px-3 py-2.5 text-sm transition-colors',
                      checked
                        ? 'border-brand-400 bg-brand-50 font-medium text-brand-800'
                        : 'border-navy-100 bg-surface-card text-navy-700 hover:border-navy-200 hover:bg-surface',
                    ].join(' ')}
                  >
                    <input
                      type="checkbox"
                      className="sr-only"
                      checked={checked}
                      onChange={() => toggleCategory(cat.id)}
                    />
                    <span
                      className={[
                        'flex h-4 w-4 shrink-0 items-center justify-center rounded border transition-colors',
                        checked
                          ? 'border-brand-500 bg-brand-500 text-white'
                          : 'border-navy-300 bg-white',
                      ].join(' ')}
                      aria-hidden
                    >
                      {checked && <CheckIcon />}
                    </span>
                    {cat.name}
                  </label>
                )
              })}
            </div>
          )}
        </section>
      </fieldset>

      {serverError && (
        <p role="alert" className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
          {serverError}
        </p>
      )}

      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full rounded-lg bg-brand-500 px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isSubmitting ? 'Zapisywanie...' : profile ? 'Zapisz zmiany' : 'Utwórz profil'}
      </button>
    </form>
  )
}

interface SectionHeaderProps {
  title: string
  subtitle: string
}

function SectionHeader({ title, subtitle }: SectionHeaderProps) {
  return (
    <div className="border-b border-navy-100 pb-3">
      <h3 className="text-sm font-semibold text-navy-900">{title}</h3>
      <p className="mt-0.5 text-xs text-muted">{subtitle}</p>
    </div>
  )
}

function CheckIcon() {
  return (
    <svg
      width="10"
      height="10"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="3.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <polyline points="20 6 9 17 4 12" />
    </svg>
  )
}
