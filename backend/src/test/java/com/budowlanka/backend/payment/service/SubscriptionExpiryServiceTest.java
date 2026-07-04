package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.service.SubscriptionExpiryService.ExpiryResult;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionExpiryServiceTest {

  @Mock private CrewSubscriptionRepository crewSubscriptionRepository;
  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private CrewBoostRepository crewBoostRepository;

  private SubscriptionExpiryService service;

  @BeforeEach
  void setUp() {
    service =
        new SubscriptionExpiryService(
            crewSubscriptionRepository, crewProfileRepository, crewBoostRepository);
  }

  @Test
  void should_deactivateExpiredThenHideProfiles_and_returnCounts() {
    when(crewSubscriptionRepository.deactivateExpired(any(Instant.class))).thenReturn(3);
    when(crewProfileRepository.hideProfilesWithoutActiveSubscription(any(Instant.class)))
        .thenReturn(2);
    when(crewBoostRepository.countByExpiresAtBefore(any(Instant.class))).thenReturn(1L);

    ExpiryResult result = service.expireSubscriptions();

    assertThat(result.deactivatedSubscriptions()).isEqualTo(3);
    assertThat(result.hiddenProfiles()).isEqualTo(2);
    verify(crewSubscriptionRepository).deactivateExpired(any(Instant.class));
    verify(crewProfileRepository).hideProfilesWithoutActiveSubscription(any(Instant.class));
  }

  @Test
  void should_returnZeroCounts_when_nothingToExpire() {
    when(crewSubscriptionRepository.deactivateExpired(any(Instant.class))).thenReturn(0);
    when(crewProfileRepository.hideProfilesWithoutActiveSubscription(any(Instant.class)))
        .thenReturn(0);
    when(crewBoostRepository.countByExpiresAtBefore(any(Instant.class))).thenReturn(0L);

    ExpiryResult result = service.expireSubscriptions();

    assertThat(result.deactivatedSubscriptions()).isZero();
    assertThat(result.hiddenProfiles()).isZero();
    verifyNoMoreInteractions(crewProfileRepository);
  }
}
