import apiClient from './client'
import type {
  PaymentResponse,
  PaymentInitResponse,
  InitiateListingPaymentRequest,
  InitiateBoostPaymentRequest,
} from '../types/payment.types'
import type { SubscriptionStatusResponse } from '../types/subscription.types'

export const paymentsApi = {
  initiateListingPayment: (packageId: string) =>
    apiClient.post<PaymentInitResponse>('/payments/listing', {
      packageId,
    } satisfies InitiateListingPaymentRequest),

  initiateBoostPayment: (boostPackageId: string) =>
    apiClient.post<PaymentInitResponse>('/payments/boost', {
      boostPackageId,
    } satisfies InitiateBoostPaymentRequest),

  getMyPayments: () => apiClient.get<PaymentResponse[]>('/payments/my'),

  getMySubscription: () => apiClient.get<SubscriptionStatusResponse>('/crew/subscription/me'),
}
