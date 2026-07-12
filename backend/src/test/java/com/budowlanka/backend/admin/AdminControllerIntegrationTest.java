package com.budowlanka.backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.service.JwtService;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private PortfolioPhotoRepository photoRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private JwtService jwtService;

  private User adminUser;
  private User crewUser;
  private User clientUser;
  private CrewProfile crewProfile;
  private String adminToken;
  private String crewToken;
  private String clientToken;

  @BeforeEach
  void setUp() {
    adminUser = saveVerifiedUser("admin-" + UUID.randomUUID() + "@test.com", UserRole.ADMIN);
    crewUser = saveVerifiedUser("crew-" + UUID.randomUUID() + "@test.com", UserRole.CREW);
    clientUser = saveVerifiedUser("client-" + UUID.randomUUID() + "@test.com", UserRole.CLIENT);
    crewProfile = saveCrewProfile(crewUser);
    adminToken = jwtService.generateAccessToken(adminUser);
    crewToken = jwtService.generateAccessToken(crewUser);
    clientToken = jwtService.generateAccessToken(clientUser);
  }

  @AfterEach
  void tearDown() {
    paymentRepository.deleteAll(
        paymentRepository.findByCrewProfileIdOrderByCreatedAtDesc(crewProfile.getId()));
    photoRepository.deleteAll(
        photoRepository.findByCrewProfileIdOrderByUploadedAtDesc(crewProfile.getId()));
    crewProfileRepository.delete(crewProfile);
    userRepository.delete(crewUser);
    userRepository.delete(clientUser);
    userRepository.delete(adminUser);
  }

  // ---- GET /api/admin/moderation/photos — role checks ----

  @Test
  void should_return403_when_moderationQueueAccessedByClient() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/moderation/photos").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return403_when_moderationQueueAccessedByCrew() throws Exception {
    mockMvc
        .perform(get("/api/admin/moderation/photos").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return200_when_moderationQueueAccessedByAdmin() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/moderation/photos").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  // ---- PUT /api/admin/moderation/photos/{id} ----

  @Test
  void should_return200_when_adminApprovesPhoto() throws Exception {
    PortfolioPhoto photo = savePhoto(crewProfile);

    mockMvc
        .perform(
            put("/api/admin/moderation/photos/" + photo.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"APPROVE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moderationStatus").value("APPROVED"));
  }

  @Test
  void should_return200_when_adminRejectsPhotoWithNote() throws Exception {
    PortfolioPhoto photo = savePhoto(crewProfile);

    mockMvc
        .perform(
            put("/api/admin/moderation/photos/" + photo.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"REJECT\",\"note\":\"Nieodpowiednie treści\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.moderationStatus").value("REJECTED"));
  }

  @Test
  void should_return403_when_crewTriesToDecidePhoto() throws Exception {
    PortfolioPhoto photo = savePhoto(crewProfile);

    mockMvc
        .perform(
            put("/api/admin/moderation/photos/" + photo.getId())
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"APPROVE\"}"))
        .andExpect(status().isForbidden());
  }

  // ---- GET /api/admin/crews — role checks ----

  @Test
  void should_return403_when_crewListAccessedByClient() throws Exception {
    mockMvc
        .perform(get("/api/admin/crews").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return403_when_crewListAccessedByCrew() throws Exception {
    mockMvc
        .perform(get("/api/admin/crews").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return200_when_adminListsCrews() throws Exception {
    mockMvc
        .perform(get("/api/admin/crews").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].ownerEmail").isNotEmpty());
  }

  @Test
  void should_filterByBlocked_when_queryParamProvided() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/crews")
                .param("blocked", "false")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  // ---- PUT /api/admin/crews/{id}/block ----

  @Test
  void should_return403_when_blockAccessedByCrew() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true,\"reason\":\"Naruszenie regulaminu\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return200_when_adminBlocksCrew() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true,\"reason\":\"Naruszenie regulaminu serwisu\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blocked").value(true))
        .andExpect(jsonPath("$.visible").value(false))
        .andExpect(jsonPath("$.blockReason").value("Naruszenie regulaminu serwisu"));
  }

  @Test
  void should_return200_and_stayHidden_when_adminUnblocksCrewWithoutSubscription()
      throws Exception {
    // Ekipa nie ma aktywnej subskrypcji — po odblokowaniu widoczność wynika z subskrypcji, więc
    // profil pozostaje ukryty (REM-149).
    crewProfile.block("Stary powód");
    crewProfileRepository.save(crewProfile);

    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blocked").value(false))
        .andExpect(jsonPath("$.visible").value(false))
        .andExpect(jsonPath("$.blockReason").isEmpty());
  }

  @Test
  void should_return400_when_blockWithReasonTooShort() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true,\"reason\":\"ab\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_blockWithNullReason() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return404_when_blockUnknownCrew() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + UUID.randomUUID() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true,\"reason\":\"Naruszenie regulaminu serwisu\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return404_on_publicSlug_when_crewIsBlocked() throws Exception {
    crewProfile.block("Naruszenie regulaminu");
    crewProfileRepository.save(crewProfile);

    mockMvc
        .perform(get("/api/crew/profiles/" + crewProfile.getSlug()))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_notAppearInPublicSearch_when_crewIsBlocked() throws Exception {
    mockMvc
        .perform(
            put("/api/admin/crews/" + crewProfile.getId() + "/block")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blocked\":true,\"reason\":\"Naruszenie regulaminu serwisu\"}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/crew/profiles").param("city", "Warszawa"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.id == '" + crewProfile.getId() + "')]").doesNotExist());
  }

  // ---- GET /api/admin/payments ----

  @Test
  void should_return401_when_paymentsListAccessedByAnonymous() throws Exception {
    mockMvc.perform(get("/api/admin/payments")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_paymentsListAccessedByClient() throws Exception {
    mockMvc
        .perform(get("/api/admin/payments").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return403_when_paymentsListAccessedByCrew() throws Exception {
    mockMvc
        .perform(get("/api/admin/payments").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return200_when_adminListsPayments() throws Exception {
    savePayment(PaymentStatus.COMPLETED);

    mockMvc
        .perform(get("/api/admin/payments").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].crewCompanyName").isNotEmpty());
  }

  @Test
  void should_filterByStatus_when_queryParamProvided() throws Exception {
    savePayment(PaymentStatus.COMPLETED);
    savePayment(PaymentStatus.PENDING);

    mockMvc
        .perform(
            get("/api/admin/payments")
                .param("status", "COMPLETED")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[?(@.status != 'COMPLETED')]").doesNotExist());
  }

  // ---- GET /api/admin/stats ----

  @Test
  void should_return401_when_statsAccessedByAnonymous() throws Exception {
    mockMvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_statsAccessedByClient() throws Exception {
    mockMvc
        .perform(get("/api/admin/stats").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return403_when_statsAccessedByCrew() throws Exception {
    mockMvc
        .perform(get("/api/admin/stats").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_returnAggregatedStats_when_adminGetsStats() throws Exception {
    savePayment(PaymentStatus.COMPLETED);
    savePhoto(crewProfile);

    // Baza jest współdzielona między testami — asercje na dolne ograniczenia, nie dokładne wartości
    mockMvc
        .perform(get("/api/admin/stats").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usersByRole.CLIENT", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.usersByRole.CREW", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.usersByRole.ADMIN", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.activeSubscriptions").isNumber())
        .andExpect(jsonPath("$.totalRevenuePln").isNumber())
        .andExpect(jsonPath("$.revenueLast30Days").isNumber())
        .andExpect(jsonPath("$.crewsCount", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.visibleCrews", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.pendingModeration", greaterThanOrEqualTo(1)));
  }

  // ---- GET /api/admin/stats/revenue ----

  private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");

  @Test
  void should_return401_when_revenueAccessedByAnonymous() throws Exception {
    mockMvc.perform(get("/api/admin/stats/revenue")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_revenueAccessedByClient() throws Exception {
    mockMvc
        .perform(get("/api/admin/stats/revenue").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return403_when_revenueAccessedByCrew() throws Exception {
    mockMvc
        .perform(get("/api/admin/stats/revenue").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return30PointsEndingToday_when_daysNotProvided() throws Exception {
    LocalDate today = LocalDate.now(WARSAW);

    mockMvc
        .perform(get("/api/admin/stats/revenue").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(30))
        .andExpect(jsonPath("$[0].date").value(today.minusDays(29).toString()))
        .andExpect(jsonPath("$[29].date").value(today.toString()));
  }

  @Test
  void should_returnRequestedWindowSize_when_daysProvided() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/stats/revenue")
                .param("days", "7")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(7));
  }

  @Test
  void should_return400_when_daysBelowRange() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/stats/revenue")
                .param("days", "0")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return400_when_daysAboveRange() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/stats/revenue")
                .param("days", "366")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_groupCompletedPaymentsByDay_when_multiplePaymentsSameDay() throws Exception {
    // Baza jest współdzielona — asercja na przyrost sumy dzisiejszego dnia, nie wartość dokładną
    double before = todayRevenue();

    savePaymentWithCompletion(PaymentStatus.COMPLETED, "89.00", Instant.now());
    savePaymentWithCompletion(PaymentStatus.COMPLETED, "89.00", Instant.now());

    assertThat(todayRevenue() - before).isCloseTo(178.00, within(0.001));
  }

  @Test
  void should_ignoreNonCompletedPayments_when_computingRevenue() throws Exception {
    double before = todayRevenue();

    savePaymentWithCompletion(PaymentStatus.FAILED, "500.00", Instant.now());
    savePayment(PaymentStatus.PENDING);

    assertThat(todayRevenue() - before).isCloseTo(0.00, within(0.001));
  }

  @Test
  void should_excludePaymentsOutsideWindow_when_daysIsOne() throws Exception {
    savePaymentWithCompletion(
        PaymentStatus.COMPLETED, "89.00", Instant.now().minus(10, ChronoUnit.DAYS));

    mockMvc
        .perform(
            get("/api/admin/stats/revenue")
                .param("days", "1")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].date").value(LocalDate.now(WARSAW).toString()));
  }

  private double todayRevenue() throws Exception {
    String body =
        mockMvc
            .perform(
                get("/api/admin/stats/revenue")
                    .param("days", "1")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<Number> amounts =
        JsonPath.read(body, "$[?(@.date == '" + LocalDate.now(WARSAW) + "')].amountPln");
    assertThat(amounts).hasSize(1);
    return amounts.get(0).doubleValue();
  }

  // ---- helpers ----

  private Payment savePaymentWithCompletion(
      PaymentStatus status, String amount, Instant completedAt) {
    return paymentRepository.save(
        Payment.builder()
            .crewProfile(crewProfile)
            .amountPln(new BigDecimal(amount))
            .paymentProvider("P24")
            .status(status)
            .completedAt(completedAt)
            .paymentType(PaymentType.LISTING)
            .build());
  }

  private Payment savePayment(PaymentStatus status) {
    return paymentRepository.save(
        Payment.builder()
            .crewProfile(crewProfile)
            .amountPln(new BigDecimal("89.00"))
            .paymentProvider("P24")
            .status(status)
            .paymentType(PaymentType.LISTING)
            .build());
  }

  private User saveVerifiedUser(String email, UserRole role) {
    return userRepository.save(
        User.builder()
            .email(email)
            .passwordHash("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .role(role)
            .emailVerified(true)
            .build());
  }

  private CrewProfile saveCrewProfile(User user) {
    return crewProfileRepository.save(
        CrewProfile.builder()
            .user(user)
            .companyName("Test Ekipa Admin")
            .slug(
                "test-ekipa-admin-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .visible(true)
            .build());
  }

  private PortfolioPhoto savePhoto(CrewProfile profile) {
    return photoRepository.save(
        PortfolioPhoto.builder()
            .crewProfile(profile)
            .storageKey("crew/test/" + UUID.randomUUID() + "-orig.jpg")
            .thumbnailKey("crew/test/" + UUID.randomUUID() + "-thumb.jpg")
            .moderationStatus(ModerationStatus.PENDING)
            .caption("Zdjęcie testowe")
            .build());
  }
}
