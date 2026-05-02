import type { Voivodeship } from './crew.types'

export type ModerationDecision = 'APPROVE' | 'REJECT'

export type ModerationDecisionRequest =
  | { decision: 'APPROVE'; note?: string }
  | { decision: 'REJECT'; note: string }

export interface AdminCrewResponse {
  id: string
  companyName: string
  slug: string
  city: string
  voivodeship: Voivodeship
  ownerEmail: string
  visible: boolean
  blocked: boolean
  blockReason: string | null
  avgRating: number
  reviewCount: number
  createdAt: string
}

export type BlockCrewRequest =
  | { blocked: true; reason: string }
  | { blocked: false; reason?: string }
