import apiClient from './client'
import type { PagedReviews, ReviewRequest, ReviewResponse } from '../types/review.types'

export const reviewsApi = {
  getReviews: (slug: string, page = 0, size = 20) =>
    apiClient.get<PagedReviews>(`/crew/profiles/${encodeURIComponent(slug)}/reviews`, {
      params: { page, size: Math.min(size, 100) },
    }),

  addReview: (slug: string, data: ReviewRequest) =>
    apiClient.post<ReviewResponse>(`/crew/profiles/${encodeURIComponent(slug)}/reviews`, data),

  updateReview: (slug: string, reviewId: string, data: ReviewRequest) =>
    apiClient.put<ReviewResponse>(
      `/crew/profiles/${encodeURIComponent(slug)}/reviews/${encodeURIComponent(reviewId)}`,
      data
    ),

  deleteReview: (slug: string, reviewId: string) =>
    apiClient.delete<void>(
      `/crew/profiles/${encodeURIComponent(slug)}/reviews/${encodeURIComponent(reviewId)}`
    ),
}
