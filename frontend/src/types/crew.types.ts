import type { ServiceCategoryResponse } from './category.types'
import type { Page } from './api.types'

export type { Page }

export type Voivodeship =
  | 'DOLNOSLASKIE'
  | 'KUJAWSKO_POMORSKIE'
  | 'LUBELSKIE'
  | 'LUBUSKIE'
  | 'LODZKIE'
  | 'MALOPOLSKIE'
  | 'MAZOWIECKIE'
  | 'OPOLSKIE'
  | 'PODKARPACKIE'
  | 'PODLASKIE'
  | 'POMORSKIE'
  | 'SLASKIE'
  | 'SWIETOKRZYSKIE'
  | 'WARMINSKO_MAZURSKIE'
  | 'WIELKOPOLSKIE'
  | 'ZACHODNIOPOMORSKIE'

export interface CrewProfileResponse {
  id: string
  companyName: string
  slug: string
  description: string | null
  /** null dla niezalogowanych użytkowników */
  phone: string | null
  /** null dla niezalogowanych użytkowników */
  contactEmail: string | null
  city: string
  voivodeship: Voivodeship
  serviceRadiusKm: number | null
  nip: string | null
  avgRating: number
  reviewCount: number
  visible: boolean
  serviceCategories: ServiceCategoryResponse[]
  createdAt: string
  updatedAt: string
}

export interface CrewProfileSummaryResponse {
  id: string
  companyName: string
  slug: string
  city: string
  voivodeship: Voivodeship
  avgRating: number
  reviewCount: number
  serviceCategories: ServiceCategoryResponse[]
  boosted?: boolean
}

export interface CreateCrewProfileRequest {
  companyName: string
  description?: string
  phone?: string
  contactEmail?: string
  city: string
  voivodeship: Voivodeship
  serviceRadiusKm?: number
  nip?: string
  categoryIds?: string[]
}

export interface UpdateCrewProfileRequest {
  companyName?: string
  description?: string
  phone?: string
  contactEmail?: string
  city?: string
  voivodeship?: Voivodeship
  serviceRadiusKm?: number
  nip?: string
  categoryIds?: string[]
}

export interface CrewSearchParams {
  city?: string
  voivodeship?: Voivodeship
  categoryId?: string
  page?: number
  size?: number
}

