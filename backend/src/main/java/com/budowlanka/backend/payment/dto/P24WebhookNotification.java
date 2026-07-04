package com.budowlanka.backend.payment.dto;

/**
 * Notyfikacja transakcji wysyłana przez P24 na {@code urlStatus}. Konsumowana przez
 * WebhookController (B7); tworzona tutaj, bo {@code P24SignatureUtil} weryfikuje jej podpis.
 *
 * <p>Podpis {@code sign} liczony jest SHA384 z JSON o ustalonej kolejności pól: {@code {merchantId,
 * posId, sessionId, amount, originAmount, currency, orderId, methodId, statement, crc}}.
 */
public record P24WebhookNotification(
    int merchantId,
    int posId,
    String sessionId,
    int amount,
    int originAmount,
    String currency,
    long orderId,
    int methodId,
    String statement,
    String sign) {}
