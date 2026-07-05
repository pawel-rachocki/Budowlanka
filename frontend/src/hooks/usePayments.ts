import { useMutation, useQuery } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { paymentsApi } from '../api/payments.api'
import type { PaymentInitResponse, PaymentResponse } from '../types/payment.types'
import { extractErrorMessage } from '../utils/errorMessage'
import { useAuth } from './useAuth'
import { useToast } from './useToast'

export function useMyPayments() {
  const { user } = useAuth()

  const { data, isLoading, isFetching, error } = useQuery<PaymentResponse[], AxiosError>({
    queryKey: ['payments', 'me'],
    queryFn: () => paymentsApi.getMyPayments().then((res) => res.data),
    enabled: user?.role === 'CREW',
    staleTime: 30_000,
  })

  return {
    payments: data ?? [],
    isLoading,
    isFetching,
    error,
  }
}

// Wspólna obsługa błędu inicjacji — 502 to problem z bramką P24, wart osobnego komunikatu.
function initiationErrorMessage(err: AxiosError): string {
  if (err.response?.status === 502) {
    return 'Chwilowy problem z bramką płatności. Spróbuj ponownie.'
  }
  return extractErrorMessage(err, 'Nie udało się rozpocząć płatności')
}

export function useInitiateListingPayment() {
  const { showToast } = useToast()

  const { mutateAsync, isPending } = useMutation<PaymentInitResponse, AxiosError, string>({
    mutationFn: (packageId) =>
      paymentsApi.initiateListingPayment(packageId).then((res) => res.data),
    onSuccess: (data) => {
      // Pełne przejście do bramki Przelewy24 — invalidacja subskrypcji/płatności
      // nastąpi po powrocie na stronę statusu (F4/F5).
      window.location.href = data.redirectUrl
    },
    onError: (err) => {
      showToast(initiationErrorMessage(err), 'error')
    },
  })

  // Zwraca mutateAsync — caller musi obsłużyć odrzucony Promise (try/catch lub .catch()).
  // onError już wyświetla toast; catch służy wyłącznie do cleanup.
  return {
    initiateListingPayment: mutateAsync,
    isPending,
  }
}

export function useInitiateBoostPayment() {
  const { showToast } = useToast()

  const { mutateAsync, isPending } = useMutation<PaymentInitResponse, AxiosError, string>({
    mutationFn: (boostPackageId) =>
      paymentsApi.initiateBoostPayment(boostPackageId).then((res) => res.data),
    onSuccess: (data) => {
      window.location.href = data.redirectUrl
    },
    onError: (err) => {
      showToast(initiationErrorMessage(err), 'error')
    },
  })

  return {
    initiateBoostPayment: mutateAsync,
    isPending,
  }
}
