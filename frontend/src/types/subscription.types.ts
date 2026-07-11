export interface SubscriptionInfo {
  packageName: string
  expiresAt: string
  /** zawsze true — zwracane są tylko aktywne subskrypcje */
  active: boolean
}

export interface BoostInfo {
  boostName: string
  expiresAt: string
}

/**
 * Status subskrypcji i boosta ekipy dla dashboardu (GET /api/crew/subscription/me).
 *
 * Brak aktywnej subskrypcji zwraca obiekt „pusty" (hasActiveSubscription=false,
 * subscription=null) zamiast 404 — front pokazuje wówczas CTA „Wykup pakiet".
 */
export interface SubscriptionStatusResponse {
  hasActiveSubscription: boolean
  isVisible: boolean
  subscription: SubscriptionInfo | null
  boost: BoostInfo | null
}
