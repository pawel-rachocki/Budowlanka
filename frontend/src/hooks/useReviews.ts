import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { reviewsApi } from '../api/reviews.api'
import type { PagedReviews, ReviewRequest, ReviewResponse } from '../types/review.types'
import { extractErrorMessage } from '../utils/errorMessage'
import { useToast } from './useToast'

export function useCrewReviews(slug: string | undefined, page = 0) {
  const { data, isLoading, isFetching, error } = useQuery<PagedReviews, AxiosError>({
    queryKey: ['reviews', slug, page],
    queryFn: () => reviewsApi.getReviews(slug!, page).then((res) => res.data),
    enabled: !!slug,
    staleTime: 60_000,
  })

  return {
    reviews: data?.content ?? [],
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    isLoading,
    isFetching,
    error,
  }
}

export function useAddReview(slug: string) {
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const { mutateAsync, isPending, error } = useMutation<ReviewResponse, AxiosError, ReviewRequest>({
    mutationFn: (data) => reviewsApi.addReview(slug, data).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', slug] })
      queryClient.invalidateQueries({ queryKey: ['crew-profile', slug] })
      showToast('Opinia dodana', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się dodać opinii'), 'error')
    },
  })

  // Zwraca mutateAsync — caller musi obsłużyć odrzucony Promise (try/catch lub .catch()).
  // onError już wyświetla toast; catch służy wyłącznie do cleanup, nie do kolejnego komunikatu błędu.
  // error?.response?.status === 409 oznacza duplikat opinii (użytkownik już ocenił tę ekipę).
  return {
    addReview: mutateAsync,
    isAdding: isPending,
    error,
  }
}

export function useUpdateReview(slug: string) {
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const { mutateAsync, isPending, error } = useMutation<
    ReviewResponse,
    AxiosError,
    { reviewId: string; data: ReviewRequest }
  >({
    mutationFn: ({ reviewId, data }) =>
      reviewsApi.updateReview(slug, reviewId, data).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', slug] })
      queryClient.invalidateQueries({ queryKey: ['crew-profile', slug] })
      showToast('Opinia zaktualizowana', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się zaktualizować opinii'), 'error')
    },
  })

  // Zwraca mutateAsync — caller musi obsłużyć odrzucony Promise (try/catch lub .catch()).
  // onError już wyświetla toast; catch służy wyłącznie do cleanup, nie do kolejnego komunikatu błędu.
  // error?.response?.status === 403 oznacza brak uprawnień do edycji tej opinii.
  return {
    updateReview: mutateAsync,
    isUpdating: isPending,
    error,
  }
}

export function useDeleteReview(slug: string) {
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  // fire-and-forget (mutate, nie mutateAsync) — spójne z useDeletePhoto; błędy obsługuje wyłącznie onError
  const { mutate, isPending } = useMutation({
    mutationFn: (reviewId: string) => reviewsApi.deleteReview(slug, reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['reviews', slug] })
      queryClient.invalidateQueries({ queryKey: ['crew-profile', slug] })
      showToast('Opinia usunięta', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się usunąć opinii'), 'error')
    },
  })

  return {
    deleteReview: mutate,
    isDeleting: isPending,
  }
}
