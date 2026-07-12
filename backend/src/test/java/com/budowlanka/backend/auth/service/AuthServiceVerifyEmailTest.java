package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.exception.VerificationTokenException;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceVerifyEmailTest {

  private static final String PLAIN_TOKEN = "testPlainTokenForVerification123456789012345";
  private static final String HASHED_TOKEN = TokenHashUtils.hash(PLAIN_TOKEN);

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
            "http://localhost:8080",
            true,
            null);
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
  void should_setEmailVerifiedAndClearToken_when_validToken() {
    User user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(false)
            .verificationToken(HASHED_TOKEN)
            .tokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();
    when(userRepository.findByVerificationToken(HASHED_TOKEN)).thenReturn(Optional.of(user));

    authService.verifyEmail(PLAIN_TOKEN);

    ArgumentCaptor<User> captor = forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.isEmailVerified()).isTrue();
    assertThat(saved.getVerificationToken()).isNull();
    assertThat(saved.getTokenExpiresAt()).isNull();
  }

  @Test
  void verifyEmail_tokenNotFound_throwsVerificationTokenException() {
    when(userRepository.findByVerificationToken(HASHED_TOKEN)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.verifyEmail(PLAIN_TOKEN))
        .isInstanceOf(VerificationTokenException.class)
        .hasMessageContaining("nieprawidłowy");
  }

  @Test
  void verifyEmail_expiredToken_throwsVerificationTokenException() {
    User user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(false)
            .verificationToken(HASHED_TOKEN)
            .tokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .build();
    when(userRepository.findByVerificationToken(HASHED_TOKEN)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.verifyEmail(PLAIN_TOKEN))
        .isInstanceOf(VerificationTokenException.class)
        .hasMessageContaining("wygasł");
  }

  @Test
  void should_notSave_when_emailAlreadyVerified() {
    User user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .verificationToken(HASHED_TOKEN)
            .tokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();
    when(userRepository.findByVerificationToken(HASHED_TOKEN)).thenReturn(Optional.of(user));

    authService.verifyEmail(PLAIN_TOKEN);

    verify(userRepository, never()).save(any());
  }

  @Test
  void verifyEmail_nullTokenExpiresAt_throwsVerificationTokenException() {
    User user =
        User.builder()
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(false)
            .verificationToken(HASHED_TOKEN)
            .tokenExpiresAt(null)
            .build();
    when(userRepository.findByVerificationToken(HASHED_TOKEN)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.verifyEmail(PLAIN_TOKEN))
        .isInstanceOf(VerificationTokenException.class)
        .hasMessageContaining("wygasł");
  }
}
