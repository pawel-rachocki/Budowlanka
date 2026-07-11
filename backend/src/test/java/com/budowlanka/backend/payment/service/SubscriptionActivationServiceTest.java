package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.CrewBoost;
import com.budowlanka.backend.payment.entity.CrewSubscription;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionActivationServiceTest {

  @Mock private ListingPackageRepository listingPackageRepository;
  @Mock private BoostPackageRepository boostPackageRepository;
  @Mock private CrewSubscriptionRepository crewSubscriptionRepository;
  @Mock private CrewBoostRepository crewBoostRepository;

  private SubscriptionActivationService service;

  private final UUID crewId = UUID.randomUUID();
  private final UUID packageId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service =
        new SubscriptionActivationService(
            listingPackageRepository,
            boostPackageRepository,
            crewSubscriptionRepository,
            crewBoostRepository);
  }

  // ── LISTING ────────────────────────────────────────────────────────────────

  @Test
  void should_createSubscription_and_setVisible_when_listing_and_noActiveSub() {
    CrewProfile crew = crew(false);
    Payment payment = listingPayment(crew);
    stubListingPackage(30);
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                any(), any()))
        .thenReturn(Optional.empty());
    UUID newSubId = stubSubscriptionSaveWithId();

    Instant before = Instant.now();
    service.activate(payment);
    Instant after = Instant.now();

    ArgumentCaptor<CrewSubscription> captor = ArgumentCaptor.forClass(CrewSubscription.class);
    verify(crewSubscriptionRepository).save(captor.capture());
    CrewSubscription saved = captor.getValue();
    assertThat(saved.getCrewProfile()).isSameAs(crew);
    assertThat(saved.isActive()).isTrue();
    assertThat(saved.getExpiresAt())
        .isBetween(before.plus(Duration.ofDays(30)), after.plus(Duration.ofDays(30)));
    assertThat(crew.isVisible()).isTrue();
    assertThat(payment.getReferenceId()).isEqualTo(newSubId);
  }

  @Test
  void should_extendExistingSubscription_when_listing_and_activeSubExists() {
    CrewProfile crew = crew(false);
    Payment payment = listingPayment(crew);
    stubListingPackage(30);
    Instant currentExpiry = Instant.now().plus(Duration.ofDays(10));
    CrewSubscription existing =
        CrewSubscription.builder()
            .crewProfile(crew)
            .startsAt(Instant.now().minus(Duration.ofDays(20)))
            .expiresAt(currentExpiry)
            .active(true)
            .build();
    UUID existingId = UUID.randomUUID();
    setId(existing, existingId);
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                any(), any()))
        .thenReturn(Optional.of(existing));

    service.activate(payment);

    // przedłużenie: max(now, currentExpiry) + 30 dni == currentExpiry + 30 dni (bo w przyszłości)
    assertThat(existing.getExpiresAt())
        .isCloseTo(currentExpiry.plus(Duration.ofDays(30)), within(10, ChronoUnit.SECONDS));
    verify(crewSubscriptionRepository, never()).save(any());
    assertThat(crew.isVisible()).isTrue();
    assertThat(payment.getReferenceId()).isEqualTo(existingId);
  }

  @Test
  void should_extendFromNow_when_activeSubAlreadyExpired() {
    CrewProfile crew = crew(false);
    Payment payment = listingPayment(crew);
    stubListingPackage(30);
    // aktywna flaga, ale expiresAt w przeszłości (wyścig przed scheduled wygaszaniem)
    Instant pastExpiry = Instant.now().minus(Duration.ofDays(2));
    CrewSubscription existing =
        CrewSubscription.builder()
            .crewProfile(crew)
            .startsAt(Instant.now().minus(Duration.ofDays(40)))
            .expiresAt(pastExpiry)
            .active(true)
            .build();
    setId(existing, UUID.randomUUID());
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                any(), any()))
        .thenReturn(Optional.of(existing));

    Instant before = Instant.now();
    service.activate(payment);
    Instant after = Instant.now();

    // liczone od NOW, nie od przeszłej daty
    assertThat(existing.getExpiresAt())
        .isBetween(before.plus(Duration.ofDays(30)), after.plus(Duration.ofDays(30)));
  }

  @Test
  void should_notSetVisible_when_crewBlocked() {
    CrewProfile crew = crew(true);
    Payment payment = listingPayment(crew);
    stubListingPackage(30);
    when(crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                any(), any()))
        .thenReturn(Optional.empty());
    stubSubscriptionSaveWithId();

    service.activate(payment);

    assertThat(crew.isVisible()).isFalse();
  }

  // ── BOOST ──────────────────────────────────────────────────────────────────

  @Test
  void should_createBoost_and_relinkReferenceId_and_notTouchVisible_when_boost() {
    CrewProfile crew = crew(false);
    Payment payment = boostPayment(crew);
    BoostPackage pkg = Mockito.mock(BoostPackage.class);
    when(pkg.getDurationDays()).thenReturn(7);
    when(boostPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            any(), any()))
        .thenReturn(Optional.empty());
    UUID newBoostId = UUID.randomUUID();
    when(crewBoostRepository.save(any(CrewBoost.class)))
        .thenAnswer(
            invocation -> {
              CrewBoost arg = invocation.getArgument(0);
              setId(arg, newBoostId);
              return arg;
            });

    Instant before = Instant.now();
    service.activate(payment);
    Instant after = Instant.now();

    ArgumentCaptor<CrewBoost> captor = ArgumentCaptor.forClass(CrewBoost.class);
    verify(crewBoostRepository).save(captor.capture());
    CrewBoost persisted = captor.getValue();
    assertThat(persisted.getCrewProfile()).isSameAs(crew);
    assertThat(persisted.getExpiresAt())
        .isBetween(before.plus(Duration.ofDays(7)), after.plus(Duration.ofDays(7)));
    assertThat(payment.getReferenceId()).isEqualTo(newBoostId);
    assertThat(crew.isVisible()).isFalse();
    verify(crewSubscriptionRepository, never()).save(any());
  }

  @Test
  void should_extendExistingBoost_when_activeBoostExists() {
    CrewProfile crew = crew(false);
    Payment payment = boostPayment(crew);
    BoostPackage pkg = Mockito.mock(BoostPackage.class);
    when(pkg.getDurationDays()).thenReturn(30);
    when(boostPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    Instant currentExpiry = Instant.now().plus(Duration.ofDays(10));
    CrewBoost existing =
        CrewBoost.builder()
            .crewProfile(crew)
            .startsAt(Instant.now().minus(Duration.ofDays(20)))
            .expiresAt(currentExpiry)
            .build();
    UUID existingId = UUID.randomUUID();
    setId(existing, existingId);
    when(crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
            any(), any()))
        .thenReturn(Optional.of(existing));

    service.activate(payment);

    // przedłużenie: max(now, currentExpiry) + 30 dni == currentExpiry + 30 dni (bo w przyszłości)
    assertThat(existing.getExpiresAt())
        .isCloseTo(currentExpiry.plus(Duration.ofDays(30)), within(10, ChronoUnit.SECONDS));
    verify(crewBoostRepository, never()).save(any());
    assertThat(payment.getReferenceId()).isEqualTo(existingId);
    assertThat(crew.isVisible()).isFalse();
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private CrewProfile crew(boolean blocked) {
    CrewProfile crew =
        CrewProfile.builder()
            .companyName("Kowalski Remonty")
            .slug("kowalski-remonty-warszawa")
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .blocked(blocked)
            .visible(false)
            .build();
    setId(crew, crewId);
    return crew;
  }

  private Payment listingPayment(CrewProfile crew) {
    return Payment.builder()
        .crewProfile(crew)
        .paymentType(PaymentType.LISTING)
        .referenceId(packageId)
        .build();
  }

  private Payment boostPayment(CrewProfile crew) {
    return Payment.builder()
        .crewProfile(crew)
        .paymentType(PaymentType.BOOST)
        .referenceId(packageId)
        .build();
  }

  private void stubListingPackage(int durationDays) {
    ListingPackage pkg = Mockito.mock(ListingPackage.class);
    when(pkg.getDurationDays()).thenReturn(durationDays);
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
  }

  /** Stub save() zwraca subskrypcję z nadanym id (Hibernate zrobiłby to na produkcji). */
  private UUID stubSubscriptionSaveWithId() {
    UUID id = UUID.randomUUID();
    when(crewSubscriptionRepository.save(any(CrewSubscription.class)))
        .thenAnswer(
            invocation -> {
              CrewSubscription arg = invocation.getArgument(0);
              setId(arg, id);
              return arg;
            });
    return id;
  }

  /** Ustawia prywatne, generowane pole {@code id} przez refleksję (na potrzeby asercji). */
  private static void setId(Object entity, UUID id) {
    try {
      var field = entity.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(entity, id);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }
}
