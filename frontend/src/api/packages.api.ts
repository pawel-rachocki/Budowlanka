import apiClient from './client'
import type { ListingPackage, BoostPackage } from '../types/package.types'

export const packagesApi = {
  getListingPackages: () => apiClient.get<ListingPackage[]>('/packages/listing'),

  getBoostPackages: () => apiClient.get<BoostPackage[]>('/packages/boost'),
}
