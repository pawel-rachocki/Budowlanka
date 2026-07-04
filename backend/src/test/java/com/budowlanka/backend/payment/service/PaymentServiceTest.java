package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24RegisterRequest;
import com.budowlanka.backend.payment.dto.P24RegisterResult;
import com.budowlanka.backend.payment.dto.PaymentInitResponse;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.exception.P24ClientException;
import com.budowlanka.backend.payment.exception.PackageNotFoundException;
import com.budowlanka.backend.payment.mapper.PaymentMapper;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private ListingPackageRepository listingPackageRepository;
  @Mock private BoostPackageRepository boostPackageRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private Przelewy24Client przelewy24Client;
  @Mock private PaymentMapper paymentMapper;

  private PaymentService service;

  private final UUID userId = UUID.randomUUID();
  private final UUID crewId = UUID.randomUUID();
  private final UUID paymentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service =
        new PaymentService(
            crewProfileRepository,
            listingPackageRepository,
            boostPackageRepository,
            paymentRepository,
            przelewy24Client,
            paymentMapper);
  }

  private User user() {
    User user = org.mockito.Mockito.mock(User.class);
    lenient().when(user.getId()).thenReturn(userId);
    lenient().when(user.getEmail()).thenReturn("ekipa@example.com");
    return user;
  }

  private CrewProfile crew() {
    CrewProfile crew = org.mockito.Mockito.mock(CrewProfile.class);
    lenient().when(crew.getId()).thenReturn(crewId);
    lenient().when(crew.getCompanyName()).thenReturn("Kowalski Remonty");
    return crew;
  }

  /** Stubs saveAndFlush to return a Payment carrying an id (Hibernate would assign it in prod). */
  private void stubSaveReturnsPaymentWithId() {
    Payment saved = org.mockito.Mockito.mock(Payment.class);
    lenient().when(saved.getId()).thenReturn(paymentId);
    when(paymentRepository.saveAndFlush(any(Payment.class))).thenReturn(saved);
  }

  // ── initiateListingPayment ─────────────────────────────────────────────────

  @Test
  void should_returnRedirectUrl_when_listingPackageValid() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.isActive()).thenReturn(true);
    when(pkg.getName()).thenReturn("30 dni");
    when(pkg.getPricePln()).thenReturn(new BigDecimal("89.00"));
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    stubSaveReturnsPaymentWithId();
    when(przelewy24Client.registerTransaction(any()))
        .thenReturn(new P24RegisterResult("tok-1", "https://p24/trnRequest/tok-1"));

    PaymentInitResponse response = service.initiateListingPayment(user(), packageId);

    assertThat(response.redirectUrl()).isEqualTo("https://p24/trnRequest/tok-1");
  }

  @Test
  void should_persistPendingListingPayment_when_initiating() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.isActive()).thenReturn(true);
    when(pkg.getName()).thenReturn("30 dni");
    when(pkg.getPricePln()).thenReturn(new BigDecimal("89.00"));
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    stubSaveReturnsPaymentWithId();
    when(przelewy24Client.registerTransaction(any()))
        .thenReturn(new P24RegisterResult("tok-1", "https://p24/trnRequest/tok-1"));

    service.initiateListingPayment(user(), packageId);

    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).saveAndFlush(captor.capture());
    Payment persisted = captor.getValue();
    assertThat(persisted.getPaymentType()).isEqualTo(PaymentType.LISTING);
    assertThat(persisted.getAmountPln()).isEqualByComparingTo("89.00");
    assertThat(persisted.getCurrency()).isEqualTo("PLN");
    assertThat(persisted.getPaymentProvider()).isEqualTo("Przelewy24");
    assertThat(persisted.getReferenceId()).isEqualTo(packageId);
    assertThat(persisted.getProviderTxId()).isNull();
  }

  @Test
  void should_sendSessionIdAndAmountInGrosze_when_registeringTransaction() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.isActive()).thenReturn(true);
    when(pkg.getName()).thenReturn("30 dni");
    when(pkg.getPricePln()).thenReturn(new BigDecimal("89.00"));
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    stubSaveReturnsPaymentWithId();
    when(przelewy24Client.registerTransaction(any()))
        .thenReturn(new P24RegisterResult("tok-1", "https://p24/trnRequest/tok-1"));

    service.initiateListingPayment(user(), packageId);

    ArgumentCaptor<P24RegisterRequest> captor = ArgumentCaptor.forClass(P24RegisterRequest.class);
    verify(przelewy24Client).registerTransaction(captor.capture());
    P24RegisterRequest req = captor.getValue();
    assertThat(req.sessionId()).isEqualTo(paymentId.toString());
    assertThat(req.amount()).isEqualTo(8900);
    assertThat(req.currency()).isEqualTo("PLN");
    assertThat(req.email()).isEqualTo("ekipa@example.com");
  }

  @Test
  void should_throwPackageNotFound_when_listingPackageMissing() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.empty());

    User user = user();
    assertThatThrownBy(() -> service.initiateListingPayment(user, packageId))
        .isInstanceOf(PackageNotFoundException.class);
    verify(paymentRepository, never()).saveAndFlush(any());
    verify(przelewy24Client, never()).registerTransaction(any());
  }

  @Test
  void should_throwPackageNotFound_when_listingPackageInactive() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.isActive()).thenReturn(false);
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));

    User user = user();
    assertThatThrownBy(() -> service.initiateListingPayment(user, packageId))
        .isInstanceOf(PackageNotFoundException.class);
    verify(przelewy24Client, never()).registerTransaction(any());
  }

  @Test
  void should_throwCrewProfileNotFound_when_noProfile() {
    UUID packageId = UUID.randomUUID();
    User user = org.mockito.Mockito.mock(User.class);
    when(user.getId()).thenReturn(userId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.initiateListingPayment(user, packageId))
        .isInstanceOf(CrewProfileNotFoundException.class);
    verify(listingPackageRepository, never()).findById(any());
  }

  @Test
  void should_propagateAndNotReturn_when_p24Fails() {
    UUID packageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    ListingPackage pkg = org.mockito.Mockito.mock(ListingPackage.class);
    when(pkg.isActive()).thenReturn(true);
    when(pkg.getName()).thenReturn("30 dni");
    when(pkg.getPricePln()).thenReturn(new BigDecimal("89.00"));
    when(listingPackageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
    stubSaveReturnsPaymentWithId();
    when(przelewy24Client.registerTransaction(any())).thenThrow(new P24ClientException("boom"));

    User user = user();
    // W produkcji @Transactional wycofuje osierocony rekord PENDING; tu weryfikujemy propagację.
    assertThatThrownBy(() -> service.initiateListingPayment(user, packageId))
        .isInstanceOf(P24ClientException.class);
  }

  // ── initiateBoostPayment ───────────────────────────────────────────────────

  @Test
  void should_returnRedirectUrl_when_boostPackageValid() {
    UUID boostPackageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    BoostPackage pkg = org.mockito.Mockito.mock(BoostPackage.class);
    when(pkg.isActive()).thenReturn(true);
    when(pkg.getName()).thenReturn("Boost 7 dni");
    when(pkg.getPricePln()).thenReturn(new BigDecimal("19.00"));
    when(boostPackageRepository.findById(boostPackageId)).thenReturn(Optional.of(pkg));
    stubSaveReturnsPaymentWithId();
    when(przelewy24Client.registerTransaction(any()))
        .thenReturn(new P24RegisterResult("tok-2", "https://p24/trnRequest/tok-2"));

    PaymentInitResponse response = service.initiateBoostPayment(user(), boostPackageId);

    assertThat(response.redirectUrl()).isEqualTo("https://p24/trnRequest/tok-2");
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository).saveAndFlush(captor.capture());
    assertThat(captor.getValue().getPaymentType()).isEqualTo(PaymentType.BOOST);
    assertThat(captor.getValue().getReferenceId()).isEqualTo(boostPackageId);
  }

  @Test
  void should_throwPackageNotFound_when_boostPackageMissing() {
    UUID boostPackageId = UUID.randomUUID();
    CrewProfile crew = crew();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crew));
    when(boostPackageRepository.findById(boostPackageId)).thenReturn(Optional.empty());

    User user = user();
    assertThatThrownBy(() -> service.initiateBoostPayment(user, boostPackageId))
        .isInstanceOf(PackageNotFoundException.class);
    verify(przelewy24Client, never()).registerTransaction(any());
  }
}
