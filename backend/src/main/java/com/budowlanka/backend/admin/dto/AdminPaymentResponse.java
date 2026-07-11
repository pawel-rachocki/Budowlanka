package com.budowlanka.backend.admin.dto;

import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pozycja listy płatności w panelu admina (GET /api/admin/payments).
 *
 * @param id identyfikator płatności
 * @param crewCompanyName nazwa ekipy, której dotyczy płatność
 * @param amountPln kwota w PLN
 * @param paymentType typ płatności — LISTING lub BOOST
 * @param status status płatności — PENDING/COMPLETED/FAILED/REFUNDED
 * @param providerTxId identyfikator transakcji u operatora (null dopóki nie zaksięgowana)
 * @param createdAt data utworzenia
 * @param completedAt data zaksięgowania (null dopóki nie COMPLETED)
 */
public record AdminPaymentResponse(
    UUID id,
    String crewCompanyName,
    BigDecimal amountPln,
    PaymentType paymentType,
    PaymentStatus status,
    String providerTxId,
    Instant createdAt,
    Instant completedAt) {}
