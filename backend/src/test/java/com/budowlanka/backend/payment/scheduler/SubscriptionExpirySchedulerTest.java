package com.budowlanka.backend.payment.scheduler;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.payment.service.SubscriptionExpiryService;
import com.budowlanka.backend.payment.service.SubscriptionExpiryService.ExpiryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cienki wrapper schedulera — cała logika jest w {@link SubscriptionExpiryService}. Testujemy
 * delegację oraz gwarancję, że wyjątek serwisu nie wywala joba (jest łapany i logowany).
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionExpirySchedulerTest {

  @Mock private SubscriptionExpiryService subscriptionExpiryService;

  private SubscriptionExpiryScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new SubscriptionExpiryScheduler(subscriptionExpiryService);
  }

  @Test
  void should_delegateToService_when_scheduledRun() {
    when(subscriptionExpiryService.expireSubscriptions()).thenReturn(new ExpiryResult(2, 1));

    scheduler.run();

    verify(subscriptionExpiryService, times(1)).expireSubscriptions();
  }

  @Test
  void should_swallowException_when_serviceThrows() {
    when(subscriptionExpiryService.expireSubscriptions())
        .thenThrow(new RuntimeException("DB down"));

    // Job nie może propagować wyjątku — kolejny cykl spróbuje ponownie.
    assertThatCode(() -> scheduler.run()).doesNotThrowAnyException();
    verify(subscriptionExpiryService).expireSubscriptions();
  }
}
