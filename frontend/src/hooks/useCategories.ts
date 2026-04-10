import { useQuery } from '@tanstack/react-query'
import { categoriesApi } from '../api/categories.api'
import type { ServiceCategoryResponse } from '../types/category.types'

export function useCategories() {
  const { data, isLoading, error } = useQuery<ServiceCategoryResponse[]>({
    queryKey: ['categories'],
    queryFn: () => categoriesApi.getCategories().then((res) => res.data),
    staleTime: Infinity,
  })

  return {
    categories: data ?? [],
    isLoading,
    error,
  }
}
