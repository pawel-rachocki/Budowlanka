package com.budowlanka.backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private CrewSubscriptionRepository crewSubscriptionRepository;
  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private PortfolioPhotoRepository portfolioPhotoRepository;

  private AdminStatsService service;

  @BeforeEach
  void setUp() {
    service =
        new AdminStatsService(
            userRepository,
            paymentRepository,
            crewSubscriptionRepository,
            crewProfileRepository,
            portfolioPhotoRepository);
  }

  @Test
  void should_aggregateAllStats_when_dataPresent() {
    when(userRepository.countGroupedByRole())
        .thenReturn(
            List.of(
                roleCount(UserRole.CLIENT, 120),
                roleCount(UserRole.CREW, 45),
                roleCount(UserRole.ADMIN, 1)));
    when(crewSubscriptionRepository.countByActiveTrueAndExpiresAtAfter(any(Instant.class)))
        .thenReturn(32L);
    when(paymentRepository.sumCompletedAmountPln()).thenReturn(new BigDecimal("4250.00"));
    when(paymentRepository.sumCompletedAmountPlnSince(any(Instant.class)))
        .thenReturn(new BigDecimal("890.00"));
    when(crewProfileRepository.count()).thenReturn(45L);
    when(crewProfileRepository.countByVisibleTrue()).thenReturn(30L);
    when(portfolioPhotoRepository.countByModerationStatus(ModerationStatus.PENDING)).thenReturn(7L);

    AdminStatsResponse stats = service.getStats();

    assertThat(stats.usersByRole())
        .containsEntry(UserRole.CLIENT, 120L)
        .containsEntry(UserRole.CREW, 45L)
        .containsEntry(UserRole.ADMIN, 1L);
    assertThat(stats.activeSubscriptions()).isEqualTo(32L);
    assertThat(stats.totalRevenuePln()).isEqualByComparingTo("4250.00");
    assertThat(stats.revenueLast30Days()).isEqualByComparingTo("890.00");
    assertThat(stats.crewsCount()).isEqualTo(45L);
    assertThat(stats.visibleCrews()).isEqualTo(30L);
    assertThat(stats.pendingModeration()).isEqualTo(7L);
  }

  @Test
  void should_fillMissingRolesWithZero_when_roleHasNoUsers() {
    when(userRepository.countGroupedByRole()).thenReturn(List.of(roleCount(UserRole.CLIENT, 3)));

    AdminStatsResponse stats = service.getStats();

    assertThat(stats.usersByRole())
        .containsEntry(UserRole.CLIENT, 3L)
        .containsEntry(UserRole.CREW, 0L)
        .containsEntry(UserRole.ADMIN, 0L);
  }

  @Test
  void should_returnAllRolesWithZero_when_noUsersAtAll() {
    when(userRepository.countGroupedByRole()).thenReturn(List.of());

    AdminStatsResponse stats = service.getStats();

    assertThat(stats.usersByRole()).hasSize(UserRole.values().length);
    assertThat(stats.usersByRole().values()).containsOnly(0L);
  }

  @Test
  void should_useRolling30DayWindow_when_computingRecentRevenue() {
    when(userRepository.countGroupedByRole()).thenReturn(List.of());
    Instant before = Instant.now();

    service.getStats();

    Instant after = Instant.now();
    ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(paymentRepository).sumCompletedAmountPlnSince(sinceCaptor.capture());
    Instant since = sinceCaptor.getValue();
    assertThat(since)
        .isBetween(before.minus(30, ChronoUnit.DAYS), after.minus(30, ChronoUnit.DAYS));
  }

  // ---- getRevenueTimeline ----

  private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");

  @Test
  void should_fillMissingDaysWithZero_when_someDaysHaveNoRevenue() {
    LocalDate today = LocalDate.now(WARSAW);
    when(paymentRepository.sumCompletedAmountPlnByDaySince(any(Instant.class)))
        .thenReturn(
            List.of(dailyRevenue(today.minusDays(2), "267.00"), dailyRevenue(today, "89.00")));

    List<RevenuePointResponse> timeline = service.getRevenueTimeline(5);

    assertThat(timeline).hasSize(5);
    assertThat(timeline)
        .extracting(RevenuePointResponse::date)
        .containsExactly(
            today.minusDays(4), today.minusDays(3), today.minusDays(2), today.minusDays(1), today);
    assertThat(timeline.get(0).amountPln()).isEqualByComparingTo("0.00");
    assertThat(timeline.get(2).amountPln()).isEqualByComparingTo("267.00");
    assertThat(timeline.get(3).amountPln()).isEqualByComparingTo("0.00");
    assertThat(timeline.get(4).amountPln()).isEqualByComparingTo("89.00");
  }

  @Test
  void should_returnOnlyZeroPoints_when_noCompletedPayments() {
    when(paymentRepository.sumCompletedAmountPlnByDaySince(any(Instant.class)))
        .thenReturn(List.of());

    List<RevenuePointResponse> timeline = service.getRevenueTimeline(3);

    assertThat(timeline).hasSize(3);
    assertThat(timeline)
        .allSatisfy(point -> assertThat(point.amountPln()).isEqualByComparingTo("0.00"));
  }

  @Test
  void should_querySinceStartOfFirstWindowDay_when_computingTimeline() {
    when(paymentRepository.sumCompletedAmountPlnByDaySince(any(Instant.class)))
        .thenReturn(List.of());
    int days = 30;
    Instant expectedBefore =
        LocalDate.now(WARSAW).minusDays(days - 1L).atStartOfDay(WARSAW).toInstant();

    service.getRevenueTimeline(days);

    Instant expectedAfter =
        LocalDate.now(WARSAW).minusDays(days - 1L).atStartOfDay(WARSAW).toInstant();
    ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
    verify(paymentRepository).sumCompletedAmountPlnByDaySince(sinceCaptor.capture());
    // Dwa odczyty zegara na wypadek przejścia przez północ w trakcie testu
    assertThat(sinceCaptor.getValue()).isBetween(expectedBefore, expectedAfter);
  }

  @Test
  void should_sumAmounts_when_repositoryReturnsDuplicateDays() {
    LocalDate today = LocalDate.now(WARSAW);
    when(paymentRepository.sumCompletedAmountPlnByDaySince(any(Instant.class)))
        .thenReturn(List.of(dailyRevenue(today, "89.00"), dailyRevenue(today, "178.00")));

    List<RevenuePointResponse> timeline = service.getRevenueTimeline(2);

    assertThat(timeline).hasSize(2);
    assertThat(timeline.get(0).amountPln()).isEqualByComparingTo("0.00");
    assertThat(timeline.get(1).amountPln()).isEqualByComparingTo("267.00");
  }

  @Test
  void should_returnSinglePoint_when_windowIsOneDay() {
    when(paymentRepository.sumCompletedAmountPlnByDaySince(any(Instant.class)))
        .thenReturn(List.of());

    List<RevenuePointResponse> timeline = service.getRevenueTimeline(1);

    assertThat(timeline).hasSize(1);
    assertThat(timeline.get(0).date()).isEqualTo(LocalDate.now(WARSAW));
  }

  private PaymentRepository.DailyRevenue dailyRevenue(LocalDate day, String amount) {
    return new PaymentRepository.DailyRevenue() {
      @Override
      public LocalDate getDay() {
        return day;
      }

      @Override
      public BigDecimal getAmount() {
        return new BigDecimal(amount);
      }
    };
  }

  private UserRepository.RoleCount roleCount(UserRole role, long count) {
    return new UserRepository.RoleCount() {
      @Override
      public UserRole getRole() {
        return role;
      }

      @Override
      public long getCount() {
        return count;
      }
    };
  }
}
