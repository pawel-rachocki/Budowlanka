package com.budowlanka.backend.payment.dto;

/**
 * Parametry biznesowe rejestracji transakcji w P24. Pola techniczne (merchantId, posId, urlReturn,
 * urlStatus, sign) dokłada {@code Przelewy24Client} z {@code P24Properties}.
 *
 * @param sessionId nasze {@code payments.id} (UUID jako String) — łączy redirect, return i webhook
 * @param amount kwota w groszach (int)
 * @param currency waluta, np. "PLN"
 * @param description opis transakcji widoczny dla klienta
 * @param email email płatnika
 */
public record P24RegisterRequest(
    String sessionId, int amount, String currency, String description, String email) {}
