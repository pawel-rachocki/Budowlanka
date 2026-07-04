package com.budowlanka.backend.payment;

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
import com.budowlanka.backend.auth.service.JwtService;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.client.Przelewy24Client;
import com.budowlanka.backend.payment.dto.P24RegisterResult;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import com.budowlanka.backend.payment.repository.PaymentRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest extends IntegrationTestBase {

  private static final String REDIRECT_URL = "https://sandbox.przelewy24.pl/trnRequest/mock-token";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private ListingPackageRepository listingPackageRepository;
  @Autowired private BoostPackageRepository boostPackageRepository;
  @Autowired private JwtService jwtService;

  // Zamockowany, by testy nie zależały od konfiguracji/komunikacji z P24.
  @MockitoBean private Przelewy24Client przelewy24Client;

  private User crewUser;
  private User clientUser;
  private CrewProfile crewProfile;
  private String crewToken;
  private String clientToken;
  private ListingPackage listingPackage;
  private BoostPackage boostPackage;

  @BeforeEach
  void setUp() {
    crewUser = saveVerifiedUser("crew-" + UUID.randomUUID() + "@test.com", UserRole.CREW);
    clientUser = saveVerifiedUser("client-" + UUID.randomUUID() + "@test.com", UserRole.CLIENT);
    crewProfile = saveCrewProfile(crewUser);
    crewToken = jwtService.generateAccessToken(crewUser);
    clientToken = jwtService.generateAccessToken(clientUser);
    listingPackage = listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();
    boostPackage = boostPackageRepository.findByActiveTrueOrderByPricePlnAsc().getFirst();

    when(przelewy24Client.registerTransaction(any()))
        .thenReturn(new P24RegisterResult("mock-token", REDIRECT_URL));
  }

  @AfterEach
  void tearDown() {
    // payments nie ma ON DELETE CASCADE na crew_profile_id — usuń najpierw.
    paymentRepository.deleteAll(
        paymentRepository.findByCrewProfileIdOrderByCreatedAtDesc(crewProfile.getId()));
    crewProfileRepository.delete(crewProfile);
    userRepository.delete(crewUser);
    userRepository.delete(clientUser);
  }

  // ---- POST /api/payments/listing ----

  @Test
  void should_return200WithRedirectUrl_when_initiateListingAsCrew() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"packageId\":\"" + listingPackage.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.redirectUrl").value(REDIRECT_URL));
  }

  @Test
  void should_return401_when_initiateListingWithNoToken() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"packageId\":\"" + listingPackage.getId() + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_initiateListingAsClient() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"packageId\":\"" + listingPackage.getId() + "\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return400_when_initiateListingWithNullPackageId() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void should_return404_when_initiateListingWithUnknownPackage() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"packageId\":\"" + UUID.randomUUID() + "\"}"))
        .andExpect(status().isNotFound());
  }

  // ---- POST /api/payments/boost ----

  @Test
  void should_return200WithRedirectUrl_when_initiateBoostAsCrew() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/boost")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"boostPackageId\":\"" + boostPackage.getId() + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.redirectUrl").value(REDIRECT_URL));
  }

  @Test
  void should_return403_when_initiateBoostAsClient() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/boost")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"boostPackageId\":\"" + boostPackage.getId() + "\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return400_when_initiateBoostWithNullPackageId() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/boost")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ---- GET /api/payments/my ----

  @Test
  void should_return401_when_listMyPaymentsWithNoToken() throws Exception {
    mockMvc.perform(get("/api/payments/my")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_listMyPaymentsAsClient() throws Exception {
    mockMvc
        .perform(get("/api/payments/my").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_returnPaymentsNewestFirst_when_listMyPaymentsAsCrew() throws Exception {
    // Dwie inicjacje — druga powinna wylądować na szczycie listy.
    initiateListing();
    initiateBoost();

    mockMvc
        .perform(get("/api/payments/my").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].paymentType").value("BOOST"))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].paymentType").value("LISTING"));
  }

  @Test
  void should_returnEmptyList_when_crewHasNoPayments() throws Exception {
    mockMvc
        .perform(get("/api/payments/my").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  // ---- helpers ----

  private void initiateListing() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/listing")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"packageId\":\"" + listingPackage.getId() + "\"}"))
        .andExpect(status().isOk());
  }

  private void initiateBoost() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/boost")
                .header("Authorization", "Bearer " + crewToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"boostPackageId\":\"" + boostPackage.getId() + "\"}"))
        .andExpect(status().isOk());
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
            .companyName("Test Ekipa")
            .slug("test-ekipa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .build());
  }
}
