import { useQuery } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { crewApi } from '../api/crew.api'
import type { CrewProfileResponse } from '../types/crew.types'

export function useCrewProfile(slug: string | undefined) {
  const { data, isLoading, isFetching, error, refetch } = useQuery<CrewProfileResponse, AxiosError>({
    queryKey: ['crew-profile', slug],
    queryFn: () => crewApi.getCrewBySlug(slug!).then((res) => res.data),
    enabled: !!slug,
    staleTime: 60_000,
  })

  return {
    profile: data ?? null,
    isLoading,
    isFetching,
    error,
    refetch,
  }
}
