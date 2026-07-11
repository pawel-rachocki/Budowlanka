import { useQuery } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { packagesApi } from '../api/packages.api'
import type { BoostPackage, ListingPackage } from '../types/package.types'

// Cennik publiczny — pakiety zmieniają się rzadko, więc długi staleTime.
const PACKAGES_STALE_TIME = 5 * 60_000

export function useListingPackages() {
  const { data, isLoading, error } = useQuery<ListingPackage[], AxiosError>({
    queryKey: ['packages', 'listing'],
    queryFn: () => packagesApi.getListingPackages().then((res) => res.data),
    staleTime: PACKAGES_STALE_TIME,
  })

  return {
    packages: data ?? [],
    isLoading,
    error,
  }
}

export function useBoostPackages() {
  const { data, isLoading, error } = useQuery<BoostPackage[], AxiosError>({
    queryKey: ['packages', 'boost'],
    queryFn: () => packagesApi.getBoostPackages().then((res) => res.data),
    staleTime: PACKAGES_STALE_TIME,
  })

  return {
    packages: data ?? [],
    isLoading,
    error,
  }
}
