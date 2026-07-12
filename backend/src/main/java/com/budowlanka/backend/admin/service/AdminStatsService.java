package com.budowlanka.backend.admin.service;

import com.budowlanka.backend.admin.dto.AdminStatsResponse;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

  private static final int REVENUE_WINDOW_DAYS = 30;

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
