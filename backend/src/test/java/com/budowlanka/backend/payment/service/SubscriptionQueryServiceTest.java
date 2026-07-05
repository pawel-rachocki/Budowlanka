package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.dto.SubscriptionStatusResponse;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.CrewBoost;
import com.budowlanka.backend.payment.entity.CrewSubscription;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionQueryServiceTest {

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private CrewSubscriptionRepository crewSubscriptionRepository;
  @Mock private CrewBoostRepository crewBoostRepository;

  private SubscriptionQueryService service;

  private final UUID userId = UUID.randomUUID();
  private final UUID crewId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service =
        new SubscriptionQueryService(
            crewProfileRepository, crewSubscriptionRepository, crewBoostRepository);
  }

  private User user() {
    User user = org.mockito.Mockito.mock(User.class);
    when(user.getId()).thenReturn(userId);
    return user;
  }

  private CrewProfile crew(boolean visible) {
    CrewProfile crew = org.mockito.Mockito.mock(CrewProfile.class);
    lenient().when(crew.getId()).thenReturn(crewId);
    lenient().when(crew.isVisible()).thenReturn(visible);
    return crew;
  }

  private CrewSubscription subscription(String packageName, Instant expiresAt) {
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.getName()).thenReturn(packageName);
    CrewSubscription sub = org.mockito.Mockito.mock(CrewSubscription.class);
    when(sub.getListingPackage()).thenReturn(pkg);
    when(sub.getExpiresAt()).thenReturn(expiresAt);
    when(sub.isActive()).thenReturn(true);
    return sub;
  }

  private CrewBoost boost(String boostName, Instant expiresAt) {
    BoostPackage pkg = org.mockito.Mockito.mock(BoostPackage.class);
    when(pkg.getName()).thenReturn(boostName);
    CrewBoost boost = org.mockito.Mockito.mock(CrewBoost.class);
    when(boost.getBoostPackage()).thenReturn(pkg);
    when(boost.getExpiresAt()).thenReturn(expiresAt);
    return boost;
  }

  @Test
  void should_returnFullStatus_when_activeSubscriptionAndBoost() {
    Instant subExpires = Instant.parse("2026-08-04T12:00:00Z");
    Instant boostExpires = Instant.parse("2026-07-12T12:00:00Z");
    CrewProfile crew = crew(true);
    CrewSubscription sub = subscription("30 dni", subExpires);
    CrewBoost boost = boost("Boost 7 dni", boostExpires);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                eq(crewId), any()))
        .thenReturn(Optional.of(sub));
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            eq(crewId), any()))
        .thenReturn(Optional.of(boost));

    SubscriptionStatusResponse response = service.getStatus(user());

    assertThat(response.hasActiveSubscription()).isTrue();
    assertThat(response.isVisible()).isTrue();
    assertThat(response.subscription()).isNotNull();
    assertThat(response.subscription().packageName()).isEqualTo("30 dni");
    assertThat(response.subscription().expiresAt()).isEqualTo(subExpires);
    assertThat(response.subscription().active()).isTrue();
    assertThat(response.boost()).isNotNull();
    assertThat(response.boost().boostName()).isEqualTo("Boost 7 dni");
    assertThat(response.boost().expiresAt()).isEqualTo(boostExpires);
  }

  @Test
  void should_returnNullBoost_when_activeSubscriptionWithoutBoost() {
    CrewProfile crew = crew(true);
    CrewSubscription sub = subscription("14 dni", Instant.parse("2026-08-01T00:00:00Z"));
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                eq(crewId), any()))
        .thenReturn(Optional.of(sub));
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            eq(crewId), any()))
        .thenReturn(Optional.empty());

    SubscriptionStatusResponse response = service.getStatus(user());

    assertThat(response.hasActiveSubscription()).isTrue();
    assertThat(response.subscription()).isNotNull();
    assertThat(response.boost()).isNull();
  }

  @Test
  void should_returnEmptyStatus_when_noActiveSubscription() {
    CrewProfile crew = crew(false);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                eq(crewId), any()))
        .thenReturn(Optional.empty());
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            eq(crewId), any()))
        .thenReturn(Optional.empty());

    SubscriptionStatusResponse response = service.getStatus(user());

    assertThat(response.hasActiveSubscription()).isFalse();
    assertThat(response.isVisible()).isFalse();
    assertThat(response.subscription()).isNull();
    assertThat(response.boost()).isNull();
  }

  @Test
  void should_reflectProfileVisibility_when_visibleFlagSet() {
    CrewProfile crew = crew(true);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                eq(crewId), any()))
        .thenReturn(Optional.empty());
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            eq(crewId), any()))
        .thenReturn(Optional.empty());

    SubscriptionStatusResponse response = service.getStatus(user());

    assertThat(response.isVisible()).isTrue();
  }

  @Test
  void should_throwCrewProfileNotFound_when_noProfile() {
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

    User user = user();
    assertThatThrownBy(() -> service.getStatus(user))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }
}
