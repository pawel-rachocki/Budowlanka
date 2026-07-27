package com.budowlanka.backend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.client.P24SignatureUtil;
import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24WebhookNotification;
import com.budowlanka.backend.payment.entity.CrewSubscription;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * E2E krytycznego flow: webhook P24 → aktywacja pakietu → widoczność profilu (REM-172).
 *
 * <p>Dotąd ścieżka była pokryta wyłącznie jednostkowo (WebhookControllerTest,
 * PaymentWebhookServiceTest, SubscriptionActivationServiceTest) — na mockach. Tutaj idzie prawdziwe
 * HTTP przez cały stos aż do bazy: sprawdzamy, że po notyfikacji ekipa realnie pojawia się w
 * publicznym API.
 *
 * <p>Podpis notyfikacji liczy prawdziwy {@link P24SignatureUtil} (te same merchantId/posId/crc z
 * testowych properties, co weryfikacja w controllerze). {@link Przelewy24Client} jest mockowany —
 * nie wołamy bramki.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentWebhookIntegrationTest extends IntegrationTestBase {

  private static final String WEBHOOK_URL = "/api/payments/webhook/p24";
  private static final long ORDER_ID = 987654321L;

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private ListingPackageRepository listingPackageRepository;
  @Autowired private BoostPackageRepository boostPackageRepository;
  @Autowired private CrewSubscriptionRepository crewSubscriptionRepository;
  @Autowired private CrewBoostRepository crewBoostRepository;
  @Autowired private P24SignatureUtil signatureUtil;
  @Autowired private JdbcTemplate jdbcTemplate;

  @MockitoBean private Przelewy24Client przelewy24Client;

  private User crewUser;
  private CrewProfile crewProfile;

  @BeforeEach
  void setUp() {
    crewUser = saveVerifiedUser("crew-" + UUID.randomUUID() + "@test.com");
    // Profil startuje niewidoczny — dopiero opłacony pakiet ma go pokazać.
    crewProfile = saveCrewProfile(crewUser, false);

    when(przelewy24Client.verifyTransaction(any())).thenReturn(true);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update("DELETE FROM crew_boosts WHERE crew_profile_id = ?", crewProfile.getId());
    jdbcTemplate.update(
        "DELETE FROM crew_subscriptions WHERE crew_profile_id = ?", crewProfile.getId());
    paymentRepository.deleteAll(
        paymentRepository.findByCrewProfileIdOrderByCreatedAtDesc(crewProfile.getId()));
    crewProfileRepository.delete(crewProfile);
    userRepository.delete(crewUser);
  }

  // ---- happy path: aktywacja subskrypcji ----

  @Test
  void should_activateSubscriptionAndMakeProfileVisible_when_webhookConfirmsPayment()
      throws Exception {
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());
    Instant before = Instant.now();

    postWebhook(signedNotification(payment)).andExpect(status().isOk());

    Payment settled = reload(payment);
    assertThat(settled.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(settled.getProviderTxId()).isEqualTo(String.valueOf(ORDER_ID));
    assertThat(settled.getCompletedAt()).isNotNull();

    CrewSubscription subscription = activeSubscription();
    assertThat(subscription.getExpiresAt())
        .isCloseTo(
            before.plus(pkg.getDurationDays(), ChronoUnit.DAYS), within(1, ChronoUnit.MINUTES));
    assertThat(crewProfileRepository.findById(crewProfile.getId()).orElseThrow().isVisible())
        .isTrue();
  }

  @Test
  void should_exposeProfilePublicly_after_webhookActivation() throws Exception {
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());

    // Przed opłaceniem: profil nie istnieje dla świata zewnętrznego
    mockMvc
        .perform(get("/api/crew/profiles/" + crewProfile.getSlug()))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(get("/api/crew/profiles").param("city", "Warszawa"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == '" + crewProfile.getId() + "')]").doesNotExist());

    postWebhook(signedNotification(payment)).andExpect(status().isOk());

    mockMvc
        .perform(get("/api/crew/profiles/" + crewProfile.getSlug()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slug").value(crewProfile.getSlug()));
    mockMvc
        .perform(get("/api/crew/profiles").param("city", "Warszawa"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == '" + crewProfile.getId() + "')]").exists());
  }

  // ---- idempotentność ----

  @Test
  void should_notActivateTwice_when_webhookDeliveredTwice() throws Exception {
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());
    P24WebhookNotification notification = signedNotification(payment);

    postWebhook(notification).andExpect(status().isOk());
    Instant expiresAfterFirst = activeSubscription().getExpiresAt();
    Instant completedAfterFirst = reload(payment).getCompletedAt();

    postWebhook(notification).andExpect(status().isOk());

    // Druga notyfikacja nie przedłuża subskrypcji ani nie zmienia płatności
    assertThat(activeSubscription().getExpiresAt()).isEqualTo(expiresAfterFirst);
    assertThat(reload(payment).getCompletedAt()).isEqualTo(completedAfterFirst);
    assertThat(crewSubscriptionRepository.countByActiveTrueAndExpiresAtAfter(Instant.now()))
        .isPositive();
    assertThat(subscriptionCount()).isEqualTo(1);
  }

  // ---- ścieżki odrzucenia ----

  @Test
  void should_return400AndNotActivate_when_signatureInvalid() throws Exception {
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());
    P24WebhookNotification tampered = withSign(signedNotification(payment), "zlyPodpis");

    postWebhook(tampered).andExpect(status().isBadRequest());

    assertThat(reload(payment).getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(subscriptionCount()).isZero();
    assertThat(crewProfileRepository.findById(crewProfile.getId()).orElseThrow().isVisible())
        .isFalse();
  }

  @Test
  void should_return200AndNotActivate_when_amountDoesNotMatch() throws Exception {
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());
    // Kwota inna niż w naszej płatności, ale podpis poprawny — P24 dostaje 200, my nie aktywujemy.
    P24WebhookNotification notification = signedNotification(payment.getId().toString(), 1);

    postWebhook(notification).andExpect(status().isOk());

    assertThat(reload(payment).getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(subscriptionCount()).isZero();
    assertThat(crewProfileRepository.findById(crewProfile.getId()).orElseThrow().isVisible())
        .isFalse();
  }

  @Test
  void should_markFailedAndKeepProfileHidden_when_verifyNotConfirmed() throws Exception {
    when(przelewy24Client.verifyTransaction(any())).thenReturn(false);
    var pkg = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.LISTING, pkg.getId(), pkg.getPricePln());

    postWebhook(signedNotification(payment)).andExpect(status().isOk());

    assertThat(reload(payment).getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(subscriptionCount()).isZero();
    assertThat(crewProfileRepository.findById(crewProfile.getId()).orElseThrow().isVisible())
        .isFalse();
  }

  @Test
  void should_return200AndNotActivate_when_sessionIdUnknown() throws Exception {
    P24WebhookNotification notification = signedNotification(UUID.randomUUID().toString(), 8900);

    postWebhook(notification).andExpect(status().isOk());

    assertThat(subscriptionCount()).isZero();
  }

  // ---- boost nie dotyka widoczności ----

  @Test
  void should_activateBoostWithoutChangingVisibility_when_paymentTypeIsBoost() throws Exception {
    var pkg = boostPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    Payment payment = savePendingPayment(PaymentType.BOOST, pkg.getId(), pkg.getPricePln());

    postWebhook(signedNotification(payment)).andExpect(status().isOk());

    assertThat(reload(payment).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(
            crewBoostRepository.findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(
                crewProfile.getId(), Instant.now()))
        .isPresent();
    // BOOST podbija ranking, ale nie robi profilu widocznym — to rola pakietu LISTING
    assertThat(crewProfileRepository.findById(crewProfile.getId()).orElseThrow().isVisible())
        .isFalse();
    assertThat(subscriptionCount()).isZero();
  }

  // ---- helpers ----

  private org.springframework.test.web.servlet.ResultActions postWebhook(
      P24WebhookNotification notification) throws Exception {
    return mockMvc.perform(
        post(WEBHOOK_URL).contentType(MediaType.APPLICATION_JSON).content(toJson(notification)));
  }

  /** Notyfikacja na kwotę płatności, podpisana kluczem CRC z testowych properties. */
  private P24WebhookNotification signedNotification(Payment payment) {
    return signedNotification(payment.getId().toString(), toGrosze(payment.getAmountPln()));
  }

  private P24WebhookNotification signedNotification(String sessionId, int amountGrosze) {
    return signatureUtil.buildSignedNotification(
        sessionId, amountGrosze, amountGrosze, "PLN", ORDER_ID, 25, "platnosc");
  }

  private static P24WebhookNotification withSign(P24WebhookNotification n, String sign) {
    return new P24WebhookNotification(
        n.merchantId(),
        n.posId(),
        n.sessionId(),
        n.amount(),
        n.originAmount(),
        n.currency(),
        n.orderId(),
        n.methodId(),
        n.statement(),
        sign);
  }

  /** Ręczny JSON — kolejność i nazwy pól jak w kontrakcie P24, bez zależności od ObjectMappera. */
  private static String toJson(P24WebhookNotification n) {
    return """
        {"merchantId":%d,"posId":%d,"sessionId":"%s","amount":%d,"originAmount":%d,\
        "currency":"%s","orderId":%d,"methodId":%d,"statement":"%s","sign":"%s"}"""
        .formatted(
            n.merchantId(),
            n.posId(),
            n.sessionId(),
            n.amount(),
            n.originAmount(),
            n.currency(),
            n.orderId(),
            n.methodId(),
            n.statement(),
            n.sign());
  }

  private Payment savePendingPayment(PaymentType type, UUID referenceId, BigDecimal amountPln) {
    return paymentRepository.save(
        Payment.builder()
            .crewProfile(crewProfile)
            .amountPln(amountPln)
            .currency("PLN")
            .paymentProvider("Przelewy24")
            .paymentType(type)
            .referenceId(referenceId)
            .status(PaymentStatus.PENDING)
            .build());
  }

  private Payment reload(Payment payment) {
    return paymentRepository.findById(payment.getId()).orElseThrow();
  }

  private CrewSubscription activeSubscription() {
    return crewSubscriptionRepository
        .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
            crewProfile.getId(), Instant.now())
        .orElseThrow(() -> new AssertionError("Brak aktywnej subskrypcji dla ekipy"));
  }

  private int subscriptionCount() {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM crew_subscriptions WHERE crew_profile_id = ?",
            Integer.class,
            crewProfile.getId());
    return count == null ? 0 : count;
  }

  private static int toGrosze(BigDecimal amountPln) {
    return amountPln.movePointRight(2).intValueExact();
  }

  private User saveVerifiedUser(String email) {
    return userRepository.save(
        User.builder()
            .email(email)
            .passwordHash("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .role(UserRole.CREW)
            .emailVerified(true)
            .build());
  }

  private CrewProfile saveCrewProfile(User user, boolean visible) {
    return crewProfileRepository.save(
        CrewProfile.builder()
            .user(user)
            .companyName("Test Ekipa Webhook")
            .slug(
                "test-ekipa-webhook-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .visible(visible)
            .build());
  }
}
