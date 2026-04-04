import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useAuth } from '../hooks/useAuth'
import FormField from '../components/FormField'
import type { ApiError } from '../types/api.types'

const schema = z.object({
  email: z.string().email('Nieprawidłowy adres email'),
  password: z.string().min(1, 'Podaj hasło'),
})

type FormData = z.infer<typeof schema>

export default function LoginPage() {
  const [serverError, setServerError] = useState<string | null>(null)
  const { login } = useAuth()
  const navigate = useNavigate()

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = async (data: FormData) => {
    setServerError(null)
    try {
      await login(data)
      navigate('/dashboard')
    } catch (err) {
      if (!axios.isAxiosError(err)) {
        setServerError('Wystąpił nieoczekiwany błąd.')
        return
      }

      if (err.response?.status === 401) {
        setError('email', { message: 'Nieprawidłowy email lub hasło' })
        return
      }

      if (err.response?.status === 403) {
        setServerError('Konto nie zostało aktywowane. Sprawdź email w celu aktywacji.')
        return
      }

      const apiError = err.response?.data as ApiError | undefined
      setServerError((apiError?.message ?? 'Wystąpił błąd. Spróbuj ponownie.').slice(0, 200))
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center bg-surface px-4">
      <div className="w-full max-w-md rounded-xl border border-navy-100 bg-surface-card p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-bold text-navy-900">Zaloguj się</h1>
        <p className="mb-6 text-sm text-muted">Portal ekip remontowych</p>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
          <fieldset disabled={isSubmitting} className="contents">
            <FormField
              label="Adres email"
              id="email"
              type="email"
              autoComplete="username"
              placeholder="jan@example.com"
              error={errors.email?.message}
              {...register('email')}
            />

            <FormField
              label="Hasło"
              id="password"
              type="password"
              autoComplete="current-password"
              placeholder="Twoje hasło"
              error={errors.password?.message}
              {...register('password')}
            />
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
            {isSubmitting ? 'Logowanie...' : 'Zaloguj się'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-muted">
          Nie masz konta?{' '}
          <Link to="/register" className="font-medium text-brand-600 hover:underline">
            Zarejestruj się
          </Link>
        </p>
      </div>
    </div>
  )
}
