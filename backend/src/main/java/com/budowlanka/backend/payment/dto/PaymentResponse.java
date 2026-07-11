package com.budowlanka.backend.payment.dto;

import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pozycja historii płatności ekipy (GET /api/payments/my).
 *
 * @param id identyfikator płatności
 * @param amountPln kwota w PLN
 * @param currency waluta (zawsze "PLN" w MVP)
 * @param paymentType typ płatności — LISTING lub BOOST
 * @param status status płatności — PENDING/COMPLETED/FAILED/REFUNDED
 * @param providerTxId identyfikator transakcji u operatora (null dopóki płatność nie zaksięgowana)
 * @param createdAt data utworzenia
 * @param completedAt data zaksięgowania (null dopóki nie COMPLETED)
 */
public record PaymentResponse(
    UUID id,
    BigDecimal amountPln,
    String currency,
    PaymentType paymentType,
    PaymentStatus status,
    String providerTxId,
    Instant createdAt,
    Instant completedAt) {}
