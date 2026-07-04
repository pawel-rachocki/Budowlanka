package com.budowlanka.backend.payment.dto;

/**
 * Parametry weryfikacji transakcji w P24 (PUT /transaction/verify), wołanej z webhooka przed
 * oznaczeniem płatności jako COMPLETED.
 *
 * @param sessionId nasze {@code payments.id} (UUID jako String)
 * @param amount kwota w groszach (int)
 * @param currency waluta, np. "PLN"
 * @param orderId identyfikator zamówienia nadany przez P24 (z notyfikacji webhooka)
 */
public record P24VerifyRequest(String sessionId, int amount, String currency, long orderId) {}
