package com.budowlanka.backend.admin;

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
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
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
  void should_return200_when_adminUnblocksCrew() throws Exception {
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
        .andExpect(jsonPath("$.visible").value(true))
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

  // ---- helpers ----

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
