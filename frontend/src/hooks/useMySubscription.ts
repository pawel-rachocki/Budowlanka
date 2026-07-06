import { useQuery } from '@tanstack/react-query'
import type { UseQueryOptions } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { paymentsApi } from '../api/payments.api'
import type { SubscriptionStatusResponse } from '../types/subscription.types'
import { useAuth } from './useAuth'

interface UseMySubscriptionOptions {
  // Interwał auto-odświeżania — liczba (ms), false, lub funkcja (query) => ms | false.
  // Strona powrotu z P24 (F6) odpytuje status co kilka sekund, bo aktywacja następuje
  // asynchronicznie z webhooka; forma funkcyjna pozwala wyłączyć polling, gdy subskrypcja
  // stanie się aktywna, czytając bieżące dane zapytania.
  refetchInterval?: UseQueryOptions<SubscriptionStatusResponse, AxiosError>['refetchInterval']
}

export function useMySubscription(options?: UseMySubscriptionOptions) {
  const { user } = useAuth()

  // Endpoint zwraca 200 z „pustym" obiektem (hasActiveSubscription=false) zamiast 404,
  // więc nie potrzeba obsługi 404 jak w useMyCrewProfile.
  // QueryKey ['subscription', 'me'] jest stabilny — strona powrotu z P24 (F6)
  // invaliduje go razem z ['payments', 'me'] po zaksięgowaniu płatności.
  const { data, isLoading, error, refetch } = useQuery<SubscriptionStatusResponse, AxiosError>({
    queryKey: ['subscription', 'me'],
    queryFn: () => paymentsApi.getMySubscription().then((res) => res.data),
    enabled: user?.role === 'CREW',
    staleTime: 30_000,
    refetchInterval: options?.refetchInterval ?? false,
  })

  return {
    subscription: data ?? null,
    isLoading,
    error,
    refetch,
  }
}
