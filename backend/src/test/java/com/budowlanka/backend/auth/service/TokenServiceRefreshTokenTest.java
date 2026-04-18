package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.exception.InvalidTokenException;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TokenServiceRefreshTokenTest {

  private static final String PLAIN_TOKEN = "plain-refresh-token-for-testing-purposes-abc123";
  private static final String HASHED_TOKEN = TokenHashUtils.hash(PLAIN_TOKEN);

  @Mock private JwtService jwtService;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  private TokenService tokenService;
  private User enabledUser;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(
                "test-secret-key-at-least-32-chars!!", 900_000L, 604_800_000L),
            "http://localhost:8080",
            true);
    tokenService = new TokenService(jwtService, refreshTokenRepository, props);

    enabledUser =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build();
  }

  @Test
  void should_returnIssuedTokens_when_validRefreshToken() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));
    when(jwtService.validateRefreshToken(PLAIN_TOKEN)).thenReturn(true);
    when(jwtService.generateAccessToken(enabledUser)).thenReturn("new-access-token");
    when(jwtService.generateRefreshToken(enabledUser)).thenReturn("new-refresh-token");
    when(refreshTokenRepository.saveAndFlush(stored)).thenReturn(stored);

    IssuedTokens result = tokenService.refreshToken(PLAIN_TOKEN);

    assertThat(result.accessToken()).isEqualTo("new-access-token");
    assertThat(result.plainRefreshToken()).isEqualTo("new-refresh-token");
  }

  @Test
  void should_revokeOldTokenBeforeSavingNew_when_rotatingToken() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));
    when(jwtService.validateRefreshToken(PLAIN_TOKEN)).thenReturn(true);
    when(jwtService.generateAccessToken(enabledUser)).thenReturn("new-access");
    when(jwtService.generateRefreshToken(enabledUser)).thenReturn("new-refresh");
    when(refreshTokenRepository.saveAndFlush(stored)).thenReturn(stored);

    tokenService.refreshToken(PLAIN_TOKEN);

    assertThat(stored.isRevoked()).isTrue();
    InOrder order = inOrder(refreshTokenRepository);
    order.verify(refreshTokenRepository).saveAndFlush(stored);
    order.verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void should_throw401_when_tokenNotFound() {
    when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }

  @Test
  void should_throw401_when_tokenRevoked() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(true)
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }

  @Test
  void should_throw401_when_tokenExpiredInDb() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }

  @Test
  void should_throw401_when_jwtSignatureInvalid() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));
    when(jwtService.validateRefreshToken(PLAIN_TOKEN)).thenReturn(false);

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }

  @Test
  void should_throw401_when_concurrentRefreshCausesConstraintViolation() {
    RefreshToken stored =
        RefreshToken.builder()
            .user(enabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));
    when(jwtService.validateRefreshToken(PLAIN_TOKEN)).thenReturn(true);
    when(jwtService.generateAccessToken(enabledUser)).thenReturn("new-access");
    when(jwtService.generateRefreshToken(enabledUser)).thenReturn("new-refresh");
    when(refreshTokenRepository.saveAndFlush(stored)).thenReturn(stored);
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }

  @Test
  void should_throw401_when_userDisabled() {
    User disabledUser =
        User.builder()
            .email("disabled@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(false)
            .build();
    RefreshToken stored =
        RefreshToken.builder()
            .user(disabledUser)
            .token(HASHED_TOKEN)
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
    when(refreshTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(stored));
    when(jwtService.validateRefreshToken(PLAIN_TOKEN)).thenReturn(true);

    assertThatThrownBy(() -> tokenService.refreshToken(PLAIN_TOKEN))
        .isInstanceOf(InvalidTokenException.class)
        .hasMessageContaining("Sesja wygasła");
  }
}
