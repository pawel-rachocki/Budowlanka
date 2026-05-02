import apiClient from './client'
import type { PhotoResponse, PublicPhotoResponse } from '../types/photo.types'

export const photosApi = {
  uploadPhoto: (file: File, caption?: string, onProgress?: (pct: number) => void) => {
    const form = new FormData()
    form.append('file', file)
    if (caption !== undefined) form.append('caption', caption)
    return apiClient.post<PhotoResponse>('/crew/photos', form, {
      onUploadProgress: onProgress
        ? (e) => {
            if (e.total) {
              onProgress(Math.round((e.loaded / e.total) * 100))
            } else {
              onProgress(-1)
            }
          }
        : undefined,
    })
  },

  listMyPhotos: () => apiClient.get<PhotoResponse[]>('/crew/photos/me'),

  deletePhoto: (id: string) => apiClient.delete<void>(`/crew/photos/${id}`),

  listPublicPhotosBySlug: (slug: string) =>
    apiClient.get<PublicPhotoResponse[]>(`/crew/profiles/${encodeURIComponent(slug)}/photos`),
}
