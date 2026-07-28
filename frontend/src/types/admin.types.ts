import type { UserRole } from './auth.types'
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

/** Statystyki zbiorcze admin dashboardu (GET /api/admin/stats). */
export interface AdminStatsResponse {
  /** Backend zwraca zawsze wszystkie trzy role — brakująca rola ma wartość 0. */
  usersByRole: Record<UserRole, number>
  /** Subskrypcje aktywne i jeszcze niewygasłe. */
  activeSubscriptions: number
  /** Suma płatności COMPLETED (bez odejmowania REFUNDED). */
  totalRevenuePln: number
  /** Jak wyżej, ale tylko z ostatnich 30 dni (okno kroczące). */
  revenueLast30Days: number
  crewsCount: number
  visibleCrews: number
  /** Zdjęcia portfolio czekające na moderację. */
  pendingModeration: number
}

/** Punkt szeregu czasowego przychodów (GET /api/admin/stats/revenue). */
export interface RevenuePointResponse {
  /** Dzień kalendarzowy w formacie ISO `yyyy-MM-dd`, strefa Europe/Warsaw. */
  date: string
  /** Suma płatności COMPLETED z tego dnia — 0.00 gdy brak. */
  amountPln: number
}
