package com.budowlanka.backend.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.service.EmailService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthVerifyIntegrationTest extends com.budowlanka.backend.IntegrationTestBase {

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
  void verify_validToken_returns200WithMessage() throws Exception {
    String email = "valid@example.com";
    String plainToken = "validIntegrationToken1234567890abcdef";
    userRepository.save(
        buildUser(email, plainToken, Instant.now().plus(1, ChronoUnit.HOURS), false));

    mockMvc
        .perform(get("/api/auth/verify").param("token", plainToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Email zweryfikowany. Możesz się zalogować."));

    User verifiedUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AssertionError("User not found in DB after verification"));

    assertThat(verifiedUser.isEmailVerified()).isTrue();
    assertThat(verifiedUser.getVerificationToken()).isNull();
    assertThat(verifiedUser.getTokenExpiresAt()).isNull();
  }

  @Test
  void verify_invalidToken_returns400() throws Exception {
    mockMvc
        .perform(get("/api/auth/verify").param("token", "nonexistent-token-xyz"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void verify_expiredToken_returns400() throws Exception {
    String plainToken = "expiredIntegrationToken12345678abcd";
    userRepository.save(
        buildUser(
            "expired@example.com", plainToken, Instant.now().minus(1, ChronoUnit.HOURS), false));

    mockMvc
        .perform(get("/api/auth/verify").param("token", plainToken))
        .andExpect(status().isBadRequest());
  }

  private User buildUser(String email, String plainToken, Instant expiresAt, boolean verified) {
    createdEmails.add(email);
    return User.builder()
        .email(email)
        .passwordHash("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
        .role(UserRole.CLIENT)
        .emailVerified(verified)
        .verificationToken(sha256Base64Url(plainToken))
        .tokenExpiresAt(expiresAt)
        .build();
  }

  private static String sha256Base64Url(String token) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
