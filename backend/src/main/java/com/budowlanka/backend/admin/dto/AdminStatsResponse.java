package com.budowlanka.backend.admin.dto;

import com.budowlanka.backend.auth.enums.UserRole;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Statystyki zbiorcze dla admin dashboardu (GET /api/admin/stats).
 *
 * @param usersByRole liczba użytkowników per rola — zawsze wszystkie klucze (brakujące role = 0)
 * @param activeSubscriptions subskrypcje z {@code is_active=true} i {@code expires_at > NOW()}
 * @param totalRevenuePln suma zaksięgowanych płatności (COMPLETED), bez odejmowania REFUNDED
 * @param revenueLast30Days przychód z okna kroczącego {@code completed_at >= NOW() - 30 dni}
 * @param crewsCount liczba wszystkich profili ekip
 * @param visibleCrews liczba profili z {@code is_visible=true}
 * @param pendingModeration zdjęcia portfolio oczekujące na moderację (PENDING)
 */
public record AdminStatsResponse(
    Map<UserRole, Long> usersByRole,
    long activeSubscriptions,
    BigDecimal totalRevenuePln,
    BigDecimal revenueLast30Days,
    long crewsCount,
    long visibleCrews,
    long pendingModeration) {}
