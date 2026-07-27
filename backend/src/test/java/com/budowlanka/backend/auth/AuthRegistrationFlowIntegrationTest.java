package com.budowlanka.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.service.EmailService;
import com.budowlanka.backend.auth.util.CookieUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * E2E krytycznego flow: rejestracja → weryfikacja email → login (REM-172).
 *
 * <p>Uzupełnia {@link AuthVerifyIntegrationTest}, który testuje sam endpoint {@code /verify} na
 * userze wstawionym wprost do bazy. Tutaj przechodzimy pełną ścieżkę użytkownika przez HTTP: token
 * weryfikacyjny nie wraca w odpowiedzi (w DB leży wyłącznie jego SHA-256), więc plain token
 * przechwytujemy z linku przekazanego do {@link EmailService} — dokładnie tak, jak dostałby go
 * użytkownik mailem.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthRegistrationFlowIntegrationTest extends IntegrationTestBase {

  private static final String PASSWORD = "Haslo123!";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @MockitoBean private EmailService emailService;

  private final List<String> createdEmails = new ArrayList<>();

  @AfterEach
  void cleanup() {
    createdEmails.forEach(
        email -> userRepository.findByEmail(email).ifPresent(userRepository::delete));
    createdEmails.clear();
  }

  @Test
  void should_registerVerifyAndLogin_when_fullHappyPath() throws Exception {
    String email = uniqueEmail();

    register(email, "CLIENT").andExpect(status().isCreated());

    User registered = findUser(email);
    assertThat(registered.isEmailVerified()).isFalse();
    assertThat(registered.getVerificationToken()).isNotNull();

    mockMvc
        .perform(get("/api/auth/verify").param("token", captureVerificationToken(email)))
        .andExpect(status().isOk());

    assertThat(findUser(email).isEmailVerified()).isTrue();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson(email, PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(cookie().exists(CookieUtils.REFRESH_COOKIE_NAME))
        .andExpect(cookie().httpOnly(CookieUtils.REFRESH_COOKIE_NAME, true));
  }

  @Test
  void should_return403_when_loginBeforeVerification() throws Exception {
    String email = uniqueEmail();
    register(email, "CLIENT").andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson(email, PASSWORD)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));

    assertThat(findUser(email).isEmailVerified()).isFalse();
  }

  @Test
  void should_return401_when_loginWithWrongPasswordAfterVerification() throws Exception {
    String email = uniqueEmail();
    register(email, "CREW").andExpect(status().isCreated());
    mockMvc
        .perform(get("/api/auth/verify").param("token", captureVerificationToken(email)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson(email, "InneHaslo123!")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_return409_when_registeringDuplicateEmail() throws Exception {
    String email = uniqueEmail();
    register(email, "CLIENT").andExpect(status().isCreated());

    register(email, "CLIENT")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void should_rejectRegistration_when_roleIsAdmin() throws Exception {
    String email = uniqueEmail();

    register(email, "ADMIN").andExpect(status().isForbidden());

    assertThat(userRepository.findByEmail(email)).isEmpty();
  }

  @Test
  void should_return400_when_passwordTooWeak() throws Exception {
    String email = uniqueEmail();

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson(email, "slabe", "CLIENT")))
        .andExpect(status().isBadRequest());

    assertThat(userRepository.findByEmail(email)).isEmpty();
  }

  @Test
  void should_return400_when_verifyingTwiceWithSameToken() throws Exception {
    String email = uniqueEmail();
    register(email, "CLIENT").andExpect(status().isCreated());
    String token = captureVerificationToken(email);

    mockMvc.perform(get("/api/auth/verify").param("token", token)).andExpect(status().isOk());

    // Token jest jednorazowy — po weryfikacji kasowany z profilu, więc ponowne użycie nie trafia
    // już na żadnego usera.
    mockMvc
        .perform(get("/api/auth/verify").param("token", token))
        .andExpect(status().isBadRequest());

    assertThat(findUser(email).isEmailVerified()).isTrue();
  }

  private org.springframework.test.web.servlet.ResultActions register(String email, String role)
      throws Exception {
    createdEmails.add(email);
    return mockMvc.perform(
        post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registrationJson(email, PASSWORD, role)));
  }

  /** Wyciąga plain token z linku aktywacyjnego przekazanego do {@link EmailService}. */
  private String captureVerificationToken(String email) {
    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendVerificationEmail(eq(email), linkCaptor.capture());

    String query = URI.create(linkCaptor.getValue()).getQuery();
    assertThat(query).startsWith("token=");
    return query.substring("token=".length());
  }

  private User findUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new AssertionError("Brak użytkownika " + email + " w bazie"));
  }

  private static String uniqueEmail() {
    return "flow-" + UUID.randomUUID() + "@example.com";
  }

  private static String registrationJson(String email, String password, String role) {
    return """
        {"email":"%s","password":"%s","role":"%s"}"""
        .formatted(email, password, role);
  }

  private static String credentialsJson(String email, String password) {
    return """
        {"email":"%s","password":"%s"}"""
        .formatted(email, password);
  }
}
