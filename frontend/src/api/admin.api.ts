import apiClient from './client'
import type { Page } from '../types/crew.types'
import type { PhotoResponse, PhotoModerationItem, ModerationStatus } from '../types/photo.types'
import type {
  AdminCrewResponse,
  BlockCrewRequest,
  ModerationDecisionRequest,
} from '../types/admin.types'

export interface AdminCrewsParams {
  page?: number
  size?: number
  blocked?: boolean
}

export const adminApi = {
  listModerationQueue: (status?: ModerationStatus, page?: number, size?: number) =>
    apiClient.get<Page<PhotoModerationItem>>('/admin/moderation/photos', {
      params: { status, page, size },
    }),

  decideOnPhoto: (id: string, body: ModerationDecisionRequest) =>
    apiClient.put<PhotoResponse>(`/admin/moderation/photos/${encodeURIComponent(id)}`, body),

  listAdminCrews: (params?: AdminCrewsParams) =>
    apiClient.get<Page<AdminCrewResponse>>('/admin/crews', { params }),

  blockCrew: (id: string, body: BlockCrewRequest) =>
    apiClient.put<AdminCrewResponse>(`/admin/crews/${encodeURIComponent(id)}/block`, body),
}
