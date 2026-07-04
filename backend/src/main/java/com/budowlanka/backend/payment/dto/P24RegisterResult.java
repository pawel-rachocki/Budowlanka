package com.budowlanka.backend.payment.dto;

/**
 * Wynik rejestracji transakcji w P24.
 *
 * @param token token transakcji zwrócony przez P24 (w trybie mock — wartość zastępcza)
 * @param redirectUrl pełny adres przekierowania klienta: {@code {baseUrl}/trnRequest/{token}}
 */
public record P24RegisterResult(String token, String redirectUrl) {}
