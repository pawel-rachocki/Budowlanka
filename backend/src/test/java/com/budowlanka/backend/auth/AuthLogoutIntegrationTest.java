package com.budowlanka.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.service.EmailService;
import com.budowlanka.backend.auth.service.JwtService;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLogoutIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private JwtService jwtService;
  @MockitoBean private EmailService emailService;

  private final List<String> createdEmails = new ArrayList<>();

  @AfterEach
  void cleanup() {
    createdEmails.forEach(
        email -> userRepository.findByEmail(email).ifPresent(userRepository::delete));
    createdEmails.clear();
  }

  @Test
  void logout_validAccessToken_returns204AndRevokesRefreshToken() throws Exception {
    User user = createVerifiedUser("logout@example.com");
    String refreshJwt = jwtService.generateRefreshToken(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .token(TokenHashUtils.hash(refreshJwt))
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build());
    String accessToken = jwtService.generateAccessToken(user);

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    assertThat(refreshTokenRepository.findByUserAndRevokedFalse(user)).isEmpty();
  }

  @Test
  void logout_noAuthorizationHeader_returns401() throws Exception {
    mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
  }

  @Test
  void logout_afterLogout_refreshTokenReturns401() throws Exception {
    User user = createVerifiedUser("logout2@example.com");
    String refreshJwt = jwtService.generateRefreshToken(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .token(TokenHashUtils.hash(refreshJwt))
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build());
    String accessToken = jwtService.generateAccessToken(user);

    mockMvc
        .perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"" + refreshJwt + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  private User createVerifiedUser(String email) {
    createdEmails.add(email);
    return userRepository.save(
        User.builder()
            .email(email)
            .passwordHash("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build());
  }
}
