export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'

export type PaymentType = 'LISTING' | 'BOOST'

export interface PaymentResponse {
  id: string
  amountPln: number
  currency: string
  paymentType: PaymentType
  status: PaymentStatus
  /** null dopóki płatność nie zaksięgowana */
  providerTxId: string | null
  createdAt: string
  /** null dopóki status nie COMPLETED */
  completedAt: string | null
}

/** Odpowiedź inicjacji płatności — adres bramki P24, pod który front przekierowuje ekipę. */
export interface PaymentInitResponse {
  redirectUrl: string
}

export interface InitiateListingPaymentRequest {
  packageId: string
}

export interface InitiateBoostPaymentRequest {
  boostPackageId: string
}
