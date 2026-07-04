package com.budowlanka.backend.payment.dto;

/**
 * Odpowiedź inicjacji płatności — adres, pod który front przekierowuje klienta do bramki P24.
 *
 * @param redirectUrl pełny adres bramki: {@code {baseUrl}/trnRequest/{token}}
 */
public record PaymentInitResponse(String redirectUrl) {}
