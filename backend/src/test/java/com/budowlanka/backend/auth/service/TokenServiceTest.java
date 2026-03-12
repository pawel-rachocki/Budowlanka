package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.dto.LoginResponse;
import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  private static final long REFRESH_EXPIRATION_MS = 604_800_000L;
  private static final String EMAIL = "test@example.com";

  @Mock private JwtService jwtService;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  private TokenService tokenService;
  private User user;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(
                "test-secret-key-at-least-32-chars!!", 900_000L, REFRESH_EXPIRATION_MS),
            "http://localhost:8080");
    tokenService = new TokenService(jwtService, refreshTokenRepository, props);

    user =
        User.builder()
            .email(EMAIL)
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build();
  }

  @Test
  void should_returnLoginResponse_when_issuingTokenPair() {
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

    LoginResponse response = tokenService.issueTokenPair(user);

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
  }

  @Test
  void should_saveHashedRefreshToken_when_issuingTokenPair() {
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

    tokenService.issueTokenPair(user);

    ArgumentCaptor<RefreshToken> captor = forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());
    RefreshToken saved = captor.getValue();
    assertThat(saved.getUser()).isEqualTo(user);
    // DB stores hash, not the raw JWT
    assertThat(saved.getToken()).isEqualTo(TokenHashUtils.hash("refresh-token"));
    assertThat(saved.getToken()).isNotEqualTo("refresh-token");
  }

  @Test
  void should_setCorrectExpiresAt_when_savingRefreshToken() {
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

    Instant before = Instant.now();
    tokenService.issueTokenPair(user);
    Instant after = Instant.now();

    ArgumentCaptor<RefreshToken> captor = forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());
    Instant expiresAt = captor.getValue().getExpiresAt();
    assertThat(expiresAt)
        .isCloseTo(before.plusMillis(REFRESH_EXPIRATION_MS), within(1, ChronoUnit.SECONDS));
    assertThat(expiresAt).isBefore(after.plusMillis(REFRESH_EXPIRATION_MS).plusSeconds(1));
  }

  @Test
  void should_returnRawJwt_in_response_not_hash() {
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

    LoginResponse response = tokenService.issueTokenPair(user);

    // Client receives raw JWT, DB receives hash — must not be swapped
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.refreshToken()).isNotEqualTo(TokenHashUtils.hash("refresh-token"));
  }

  @Test
  void should_deleteOldTokens_before_issuingNew() {
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

    tokenService.issueTokenPair(user);

    InOrder inOrder = inOrder(refreshTokenRepository);
    inOrder.verify(refreshTokenRepository).deleteByUser(user);
    inOrder.verify(refreshTokenRepository).save(any());
  }
}
