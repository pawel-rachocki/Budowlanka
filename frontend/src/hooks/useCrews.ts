import { useQuery } from '@tanstack/react-query'
import { crewApi } from '../api/crew.api'
import type { CrewSearchParams } from '../types/crew.types'

export function useCrews(params: CrewSearchParams = {}) {
  const { city, voivodeship, categoryId, page = 0, size: rawSize = 20 } = params
  const size = Math.min(rawSize, 100)

  const { data, isLoading, isFetching, error } = useQuery({
    queryKey: ['crews', { city, voivodeship, categoryId, page, size }],
    queryFn: () =>
      crewApi.getCrews({ city, voivodeship, categoryId, page, size }).then((res) => res.data),
    staleTime: 30_000,
  })

  return {
    crews: data?.content ?? [],
    totalElements: data?.totalElements ?? 0,
    totalPages: data?.totalPages ?? 0,
    isLoading,
    isFetching,
    error,
  }
}
