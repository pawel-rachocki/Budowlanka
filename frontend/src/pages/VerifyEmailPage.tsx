import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import axios from 'axios'
import { authApi } from '../api/auth.api'
import type { ApiError } from '../types/api.types'

type Status = 'loading' | 'success' | 'error'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')

  const [status, setStatus] = useState<Status>(token ? 'loading' : 'error')
  const [errorMessage, setErrorMessage] = useState<string>(
    token ? '' : 'Brak tokena weryfikacyjnego w linku.'
  )

  useEffect(() => {
    if (!token) return

    authApi
      .verifyEmail(token)
      .then(() => setStatus('success'))
      .catch((err) => {
        let message = 'Wystąpił błąd. Spróbuj ponownie później.'

        if (axios.isAxiosError(err)) {
          const httpStatus = err.response?.status
          if (httpStatus === 410) {
            message = 'Link weryfikacyjny wygasł. Zarejestruj się ponownie.'
          } else if (httpStatus === 400 || httpStatus === 404) {
            message = 'Link weryfikacyjny jest nieprawidłowy.'
          } else {
            const apiError = err.response?.data as ApiError | undefined
            if (apiError?.message) message = apiError.message.slice(0, 200)
          }
        }

        setErrorMessage(message)
        setStatus('error')
      })
  }, [token])

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-4">
      <div className="w-full max-w-md rounded-xl border border-navy-100 bg-surface-card p-8 shadow-sm text-center">
        {status === 'loading' && (
          <>
            <svg
              className="mx-auto mb-4 h-10 w-10 animate-spin text-brand-500"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
              />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
              />
            </svg>
            <p className="text-navy-700 font-medium">Weryfikacja adresu email...</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-green-100">
              <svg
                className="h-7 w-7 text-green-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2.5}
                aria-hidden="true"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h1 className="mb-2 text-xl font-bold text-navy-900">Email zweryfikowany</h1>
            <p className="mb-6 text-sm text-muted">
              Twoje konto jest aktywne. Możesz się zalogować.
            </p>
            <Link
              to="/login"
              className="inline-block w-full rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-brand-600"
            >
              Przejdź do logowania
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-red-100">
              <svg
                className="h-7 w-7 text-red-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2.5}
                aria-hidden="true"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h1 className="mb-2 text-xl font-bold text-navy-900">Weryfikacja nieudana</h1>
            <p role="alert" className="mb-6 text-sm text-red-600">
              {errorMessage}
            </p>
            <Link
              to="/register"
              className="inline-block w-full rounded-lg border border-navy-100 px-4 py-2.5 text-sm font-semibold text-navy-700 transition-colors hover:bg-navy-50"
            >
              Wróć do rejestracji
            </Link>
          </>
        )}
      </div>
    </div>
  )
}
