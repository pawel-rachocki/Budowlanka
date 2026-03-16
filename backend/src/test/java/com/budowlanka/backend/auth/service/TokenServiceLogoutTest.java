package com.budowlanka.backend.auth.service;

import static org.mockito.Mockito.verify;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceLogoutTest {

  @Mock private JwtService jwtService;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  private TokenService tokenService;
  private User user;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(
                "test-secret-key-at-least-32-chars!!", 900_000L, 604_800_000L),
            "http://localhost:8080");
    tokenService = new TokenService(jwtService, refreshTokenRepository, props);

    user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build();
  }

  @Test
  void should_callRevokeAllActiveByUser_when_logout() {
    tokenService.logout(user);

    verify(refreshTokenRepository).revokeAllActiveByUser(user);
  }
}
