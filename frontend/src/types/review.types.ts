import type { Page } from './api.types'

export interface ReviewResponse {
  id: string
  rating: number
  comment: string | null
  authorDisplayName: string
  createdAt: string
}

export interface ReviewRequest {
  rating: number
  comment?: string
}

export type PagedReviews = Page<ReviewResponse>
