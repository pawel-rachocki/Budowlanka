export type ModerationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface PhotoResponse {
  id: string
  url: string
  thumbnailUrl: string | null
  caption: string | null
  moderationStatus: ModerationStatus
  moderationNote: string | null
  uploadedAt: string
}

export interface PublicPhotoResponse {
  id: string
  url: string
  thumbnailUrl: string | null
  caption: string | null
  uploadedAt: string
}

export interface PhotoModerationItem {
  id: string
  originalUrl: string
  thumbnailUrl: string | null
  caption: string | null
  crewCompanyName: string
  crewSlug: string
  uploadedAt: string
}
