package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.dto.RefreshResponse;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

  private static final String PLAIN_TOKEN = "plain-refresh-token-for-testing-purposes-abc123";

  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private EmailService emailService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private TokenService tokenService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(
                "test-secret-key-at-least-32-chars!!", 900_000L, 604_800_000L),
            "http://localhost:8080");
    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            emailService,
            props,
            authenticationManager,
            tokenService);
  }

  @Test
  void should_delegateToTokenService_when_refreshingToken() {
    when(tokenService.refreshToken(PLAIN_TOKEN))
        .thenReturn(new RefreshResponse("new-access-token"));

    RefreshResponse response = authService.refreshToken(PLAIN_TOKEN);

    assertThat(response.accessToken()).isEqualTo("new-access-token");
    verify(tokenService).refreshToken(PLAIN_TOKEN);
  }
}
