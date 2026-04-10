import apiClient from './client'
import type {
  CreateCrewProfileRequest,
  CrewProfileResponse,
  CrewProfileSummaryResponse,
  CrewSearchParams,
  Page,
  UpdateCrewProfileRequest,
} from '../types/crew.types'

export const crewApi = {
  createProfile: (data: CreateCrewProfileRequest) =>
    apiClient.post<CrewProfileResponse>('/crew/profiles', data),

  getMyProfile: () => apiClient.get<CrewProfileResponse>('/crew/profiles/me'),

  updateProfile: (data: UpdateCrewProfileRequest) =>
    apiClient.put<CrewProfileResponse>('/crew/profiles/me', data),

  getCrewBySlug: (slug: string) =>
    apiClient.get<CrewProfileResponse>(`/crew/profiles/${encodeURIComponent(slug)}`),

  getCrews: (params?: CrewSearchParams) =>
    apiClient.get<Page<CrewProfileSummaryResponse>>('/crew/profiles', { params }),
}
