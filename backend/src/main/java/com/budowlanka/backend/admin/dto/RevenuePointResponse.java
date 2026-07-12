package com.budowlanka.backend.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Punkt szeregu czasowego przychodów (GET /api/admin/stats/revenue).
 *
 * @param date dzień kalendarzowy w strefie Europe/Warsaw
 * @param amountPln suma zaksięgowanych płatności (COMPLETED) z tego dnia, 0.00 gdy brak
 */
public record RevenuePointResponse(LocalDate date, BigDecimal amountPln) {}
