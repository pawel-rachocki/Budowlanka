import { useQuery } from '@tanstack/react-query'
import { isAxiosError } from 'axios'
import type { AxiosError } from 'axios'
import { crewApi } from '../api/crew.api'
import { useAuth } from './useAuth'
import type { CrewProfileResponse } from '../types/crew.types'

export function useMyCrewProfile() {
  const { user } = useAuth()

  const { data, isLoading, error, refetch } = useQuery<CrewProfileResponse | null, AxiosError>({
    queryKey: ['my-crew-profile'],
    queryFn: async () => {
      try {
        const res = await crewApi.getMyProfile()
        return res.data
      } catch (err) {
        if (isAxiosError(err) && err.response?.status === 404) return null
        throw err
      }
    },
    enabled: user?.role === 'CREW',
    staleTime: 60_000,
  })

  return {
    profile: data ?? null,
    isLoading,
    error,
    hasProfile: data != null,
    refetch,
  }
}
