import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { authApi } from '../api/auth.api'
import FormField from '../components/FormField'
import type { ApiError } from '../types/api.types'
import type { RegisterRequest } from '../types/auth.types'

const schema = z
  .object({
    email: z.string().email('Nieprawidłowy adres email'),
    password: z
      .string()
      .min(8, 'Hasło musi mieć min. 8 znaków')
      .max(100, 'Hasło może mieć maks. 100 znaków')
      .regex(/[A-Za-z]/, 'Hasło musi zawierać min. 1 literę')
      .regex(/\d/, 'Hasło musi zawierać min. 1 cyfrę')
      .regex(/[^A-Za-z0-9]/, 'Hasło musi zawierać min. 1 znak specjalny'),
    confirmPassword: z.string(),
    role: z.enum(['CLIENT', 'CREW'], { error: 'Wybierz rolę' }),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: 'Hasła muszą być identyczne',
    path: ['confirmPassword'],
  })

type FormData = z.infer<typeof schema>

export default function RegisterPage() {
  const [serverError, setServerError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    try {
      await authApi.register({
        email: data.email,
        password: data.password,
        role: data.role,
      } satisfies RegisterRequest)
      setSuccess(true)
    } catch (err) {
      if (!axios.isAxiosError(err)) {
        setServerError('Wystąpił nieoczekiwany błąd.')
        return
      }

      const apiError = err.response?.data as ApiError | undefined

      if (err.response?.status === 409) {
        setError('email', { message: 'Ten adres email jest już zajęty' })
        return
      }

      if (apiError?.errors?.length) {
        setServerError(apiError.errors.map((e) => e.slice(0, 200)).join(', '))
        return
      }

      setServerError((apiError?.message ?? 'Wystąpił błąd. Spróbuj ponownie.').slice(0, 200))
    }
  }

  if (success) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-surface px-4">
        <div className="w-full max-w-md rounded-xl border border-navy-100 bg-surface-card p-8 text-center shadow-sm">
          <div className="mb-4 text-4xl" aria-hidden="true">
            ✉️
          </div>
          <h1 className="mb-2 text-xl font-semibold text-navy-900">Sprawdź swoją skrzynkę</h1>
          <p className="mb-6 text-navy-600">
            Rejestracja udana. Sprawdź email, aby aktywować konto.
          </p>
          <Link to="/login" className="text-sm font-medium text-brand-600 hover:underline">
            Przejdź do logowania
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-4">
      <div className="w-full max-w-md rounded-xl border border-navy-100 bg-surface-card p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-bold text-navy-900">Utwórz konto</h1>
        <p className="mb-6 text-sm text-muted">Portal ekip remontowych</p>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
          <fieldset disabled={isSubmitting} className="contents">
            <FormField
              label="Adres email"
              id="email"
              type="email"
              autoComplete="username email"
              placeholder="jan@example.com"
              error={errors.email?.message}
              {...register('email')}
            />

            <FormField
              label="Hasło"
              id="password"
              type="password"
              autoComplete="new-password"
              placeholder="Min. 8 znaków"
              error={errors.password?.message}
              {...register('password')}
            />

            <FormField
              label="Potwierdź hasło"
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              placeholder="Powtórz hasło"
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />

            <div className="flex flex-col gap-1">
              <p className="text-sm font-medium text-navy-800">Rejestruję się jako</p>
              <div className="flex gap-3">
                <label className="flex flex-1 cursor-pointer items-center gap-2 rounded-lg border border-navy-100 px-4 py-3 has-[:checked]:border-brand-500 has-[:checked]:bg-brand-50">
                  <input
                    type="radio"
                    value="CLIENT"
                    {...register('role')}
                    className="accent-brand-500"
                  />
                  <span className="text-sm font-medium text-navy-800">Klient</span>
                </label>
                <label className="flex flex-1 cursor-pointer items-center gap-2 rounded-lg border border-navy-100 px-4 py-3 has-[:checked]:border-brand-500 has-[:checked]:bg-brand-50">
                  <input
                    type="radio"
                    value="CREW"
                    {...register('role')}
                    className="accent-brand-500"
                  />
                  <span className="text-sm font-medium text-navy-800">Ekipa remontowa</span>
                </label>
              </div>
              {errors.role && (
                <p role="alert" className="text-xs text-red-500">
                  {errors.role.message}
                </p>
              )}
            </div>
          </fieldset>

          {serverError && (
            <p role="alert" className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">
              {serverError}
            </p>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="mt-2 w-full rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Rejestrowanie...' : 'Zarejestruj się'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-muted">
          Masz już konto?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Zaloguj się
          </Link>
        </p>
      </div>
    </div>
  )
}
