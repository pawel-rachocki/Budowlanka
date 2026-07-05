import { useQuery } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { paymentsApi } from '../api/payments.api'
import type { SubscriptionStatusResponse } from '../types/subscription.types'
import { useAuth } from './useAuth'

export function useMySubscription() {
  const { user } = useAuth()

  // Endpoint zwraca 200 z „pustym" obiektem (hasActiveSubscription=false) zamiast 404,
  // więc nie potrzeba obsługi 404 jak w useMyCrewProfile.
  // QueryKey ['subscription', 'me'] jest stabilny — strona powrotu z P24 (F4/F5)
  // invaliduje go razem z ['payments', 'me'] po zaksięgowaniu płatności.
  const { data, isLoading, error, refetch } = useQuery<SubscriptionStatusResponse, AxiosError>({
    queryKey: ['subscription', 'me'],
    queryFn: () => paymentsApi.getMySubscription().then((res) => res.data),
    enabled: user?.role === 'CREW',
    staleTime: 30_000,
  })

  return {
    subscription: data ?? null,
    isLoading,
    error,
    refetch,
  }
}
