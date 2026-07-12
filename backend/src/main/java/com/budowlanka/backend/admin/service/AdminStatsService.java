package com.budowlanka.backend.admin.service;

import com.budowlanka.backend.admin.dto.AdminStatsResponse;
import com.budowlanka.backend.admin.dto.RevenuePointResponse;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

  private static final int REVENUE_WINDOW_DAYS = 30;

  // Musi być zgodna ze strefą w PaymentRepository.sumCompletedAmountPlnByDaySince
  private static final ZoneId REVENUE_ZONE = ZoneId.of("Europe/Warsaw");

  // Skala 2 jak numeric(10,2) w DB — dni bez płatności serializują się jako 0.00
  private static final BigDecimal ZERO_PLN = BigDecimal.ZERO.setScale(2);

  private final UserRepository userRepository;
  private final PaymentRepository paymentRepository;
  private final CrewSubscriptionRepository crewSubscriptionRepository;
  private final CrewProfileRepository crewProfileRepository;
  private final PortfolioPhotoRepository portfolioPhotoRepository;

  @Transactional(readOnly = true)
  public AdminStatsResponse getStats() {
    Instant now = Instant.now();
    return new AdminStatsResponse(
        usersByRole(),
        crewSubscriptionRepository.countByActiveTrueAndExpiresAtAfter(now),
        paymentRepository.sumCompletedAmountPln(),
        paymentRepository.sumCompletedAmountPlnSince(
            now.minus(REVENUE_WINDOW_DAYS, ChronoUnit.DAYS)),
        crewProfileRepository.count(),
        crewProfileRepository.countByVisibleTrue(),
        portfolioPhotoRepository.countByModerationStatus(ModerationStatus.PENDING));
  }

  /**
   * Szereg czasowy przychodów: ostatnie {@code days} dni kalendarzowych (Europe/Warsaw) włącznie z
   * dzisiaj. Zawsze dokładnie {@code days} punktów — dni bez płatności mają 0.00.
   */
  @Transactional(readOnly = true)
  public List<RevenuePointResponse> getRevenueTimeline(int days) {
    LocalDate today = LocalDate.now(REVENUE_ZONE);
    LocalDate firstDay = today.minusDays(days - 1L);
    Instant since = firstDay.atStartOfDay(REVENUE_ZONE).toInstant();

    Map<LocalDate, BigDecimal> byDay =
        paymentRepository.sumCompletedAmountPlnByDaySince(since).stream()
            .collect(
                Collectors.toMap(
                    PaymentRepository.DailyRevenue::getDay,
                    PaymentRepository.DailyRevenue::getAmount,
                    BigDecimal::add));

    List<RevenuePointResponse> timeline = new ArrayList<>(days);
    for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
      timeline.add(new RevenuePointResponse(day, byDay.getOrDefault(day, ZERO_PLN)));
    }
    return timeline;
  }

  /** Mapa zawsze ze wszystkimi rolami — kontrakt gwarantuje frontendowi komplet kluczy. */
  private Map<UserRole, Long> usersByRole() {
    Map<UserRole, Long> counts = new EnumMap<>(UserRole.class);
    for (UserRole role : UserRole.values()) {
      counts.put(role, 0L);
    }
    userRepository.countGroupedByRole().forEach(rc -> counts.put(rc.getRole(), rc.getCount()));
    return counts;
  }
}
