import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { AxiosError } from 'axios'
import { photosApi } from '../api/photos.api'
import type { PhotoResponse, PublicPhotoResponse } from '../types/photo.types'
import { extractErrorMessage } from '../utils/errorMessage'
import { useAuth } from './useAuth'
import { useToast } from './useToast'

export function useCrewPhotos(slug: string | undefined) {
  const { data, isLoading, isFetching, error } = useQuery<PublicPhotoResponse[], AxiosError>({
    queryKey: ['photos', 'public', slug],
    queryFn: () => photosApi.listPublicPhotosBySlug(slug!).then((res) => res.data),
    enabled: !!slug,
    staleTime: 60_000,
  })

  return {
    photos: data ?? [],
    isLoading,
    isFetching,
    error,
  }
}

export function useMyPhotos() {
  const { user } = useAuth()

  const { data, isLoading, isFetching, error } = useQuery<PhotoResponse[], AxiosError>({
    queryKey: ['photos', 'me'],
    queryFn: () => photosApi.listMyPhotos().then((res) => res.data),
    enabled: user?.role === 'CREW',
    staleTime: 30_000,
  })

  return {
    photos: data ?? [],
    isLoading,
    isFetching,
    error,
  }
}

export function useUploadPhoto() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const { mutateAsync, isPending } = useMutation({
    mutationFn: ({
      file,
      caption,
      onProgress,
    }: {
      file: File
      caption?: string
      onProgress?: (pct: number) => void
    }) => photosApi.uploadPhoto(file, caption, onProgress).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['photos', 'me'] })
      showToast('Zdjęcie przesłane — oczekuje na moderację', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się przesłać zdjęcia'), 'error')
    },
  })

  // Zwraca mutateAsync — caller musi obsłużyć odrzucony Promise (try/catch lub .catch())
  return {
    uploadPhoto: mutateAsync,
    isUploading: isPending,
  }
}

export function useDeletePhoto() {
  const queryClient = useQueryClient()
  const { showToast } = useToast()

  const { mutate, isPending } = useMutation({
    mutationFn: (id: string) => photosApi.deletePhoto(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['photos', 'me'] })
      showToast('Zdjęcie usunięte', 'success')
    },
    onError: (err) => {
      showToast(extractErrorMessage(err, 'Nie udało się usunąć zdjęcia'), 'error')
    },
  })

  return {
    deletePhoto: mutate,
    isDeleting: isPending,
  }
}
