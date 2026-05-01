package com.budowlanka.backend.photo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import com.budowlanka.backend.photo.service.PhotoModerationService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PhotoControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private PortfolioPhotoRepository photoRepository;
  @Autowired private JwtService jwtService;
  @MockitoBean private PhotoModerationService photoModerationService;

  private User crewUser;
  private User clientUser;
  private CrewProfile crewProfile;
  private String crewToken;
  private String clientToken;
  private final List<Runnable> extraCleanups = new ArrayList<>();

  @BeforeEach
  void setUp() {
    crewUser = saveVerifiedUser("crew-" + UUID.randomUUID() + "@test.com", UserRole.CREW);
    clientUser = saveVerifiedUser("client-" + UUID.randomUUID() + "@test.com", UserRole.CLIENT);
    crewProfile = saveCrewProfile(crewUser);
    crewToken = jwtService.generateAccessToken(crewUser);
    clientToken = jwtService.generateAccessToken(clientUser);
  }

  @AfterEach
  void tearDown() {
    extraCleanups.forEach(Runnable::run);
    extraCleanups.clear();
    // ON DELETE CASCADE on portfolio_photos.crew_profile_id removes photos automatically
    crewProfileRepository.delete(crewProfile);
    userRepository.delete(crewUser);
    userRepository.delete(clientUser);
  }

  // ---- POST /api/crew/photos ----

  @Test
  void should_return401_when_uploadWithNoToken() throws Exception {
    mockMvc
        .perform(multipart("/api/crew/photos").file(anyFile()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_uploadAsClient() throws Exception {
    mockMvc
        .perform(
            multipart("/api/crew/photos")
                .file(anyFile())
                .header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return400_when_uploadInvalidMime() throws Exception {
    MockMultipartFile textFile =
        new MockMultipartFile(
            "file", "doc.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());
    mockMvc
        .perform(
            multipart("/api/crew/photos")
                .file(textFile)
                .header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value("Niedozwolony format pliku. Akceptowane: JPEG, PNG."));
  }

  @Test
  void should_return202_when_uploadValidJpeg() throws Exception {
    mockMvc
        .perform(
            multipart("/api/crew/photos")
                .file(validJpegFile())
                .param("caption", "Remont kuchni")
                .header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.moderationStatus").value("PENDING"));
  }

  // ---- GET /api/crew/photos/me ----

  @Test
  void should_return401_when_listMineWithNoToken() throws Exception {
    mockMvc.perform(get("/api/crew/photos/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_listMineAsClient() throws Exception {
    mockMvc
        .perform(get("/api/crew/photos/me").header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return200_when_listMineAsCrew() throws Exception {
    mockMvc
        .perform(get("/api/crew/photos/me").header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  // ---- DELETE /api/crew/photos/{id} ----

  @Test
  void should_return401_when_deleteWithNoToken() throws Exception {
    mockMvc
        .perform(delete("/api/crew/photos/" + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_return403_when_deleteAsClient() throws Exception {
    mockMvc
        .perform(
            delete("/api/crew/photos/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void should_return204_when_deleteOwnPhoto() throws Exception {
    PortfolioPhoto photo = savePhoto(crewProfile);

    mockMvc
        .perform(
            delete("/api/crew/photos/" + photo.getId())
                .header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isNoContent());
  }

  @Test
  void should_return403_when_deletingAnotherCrewsPhoto() throws Exception {
    User otherCrew = saveVerifiedUser("other-" + UUID.randomUUID() + "@test.com", UserRole.CREW);
    CrewProfile otherProfile = saveCrewProfile(otherCrew);
    PortfolioPhoto otherPhoto = savePhoto(otherProfile);
    extraCleanups.add(() -> crewProfileRepository.delete(otherProfile));
    extraCleanups.add(() -> userRepository.delete(otherCrew));

    mockMvc
        .perform(
            delete("/api/crew/photos/" + otherPhoto.getId())
                .header("Authorization", "Bearer " + crewToken))
        .andExpect(status().isForbidden());
  }

  // ---- GET /api/crew/profiles/{slug}/photos ----

  @Test
  void should_return200_when_listPublicApprovedPhotos() throws Exception {
    PortfolioPhoto photo = savePhoto(crewProfile);
    photo.approve();
    photoRepository.save(photo);

    mockMvc
        .perform(get("/api/crew/profiles/" + crewProfile.getSlug() + "/photos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(photo.getId().toString()))
        .andExpect(jsonPath("$[0].moderationStatus").value((Object) null));
  }

  @Test
  void should_returnEmptyList_when_allPhotosArePending() throws Exception {
    savePhoto(crewProfile); // default status = PENDING

    mockMvc
        .perform(get("/api/crew/profiles/" + crewProfile.getSlug() + "/photos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
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
            .companyName("Test Ekipa")
            .slug("test-ekipa-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8))
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .build());
  }

  private PortfolioPhoto savePhoto(CrewProfile profile) {
    return photoRepository.save(
        PortfolioPhoto.builder()
            .crewProfile(profile)
            .storageKey("crew/test/" + UUID.randomUUID() + "-orig.jpg")
            .thumbnailKey("crew/test/" + UUID.randomUUID() + "-thumb.jpg")
            .caption("Testowe zdjęcie")
            .build());
  }

  private MockMultipartFile anyFile() {
    return new MockMultipartFile(
        "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});
  }

  private MockMultipartFile validJpegFile() throws IOException {
    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", baos);
    return new MockMultipartFile(
        "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, baos.toByteArray());
  }
}
