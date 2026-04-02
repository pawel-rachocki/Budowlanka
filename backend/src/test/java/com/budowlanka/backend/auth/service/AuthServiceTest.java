package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
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
class AuthServiceTest {

  private static final String BASE_URL = "http://localhost:8080";
  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "Haslo123!";

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
            BASE_URL,
            true);
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
  void should_saveUser_when_validClientRegistration() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));

    ArgumentCaptor<User> captor = forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.getEmail()).isEqualTo(EMAIL);
    assertThat(saved.getRole()).isEqualTo(UserRole.CLIENT);
    assertThat(saved.isEmailVerified()).isFalse();
    assertThat(saved.getVerificationToken()).isNotBlank();
  }

  @Test
  void should_hashPassword_when_registering() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("bcrypt-hash");

    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));

    ArgumentCaptor<User> captor = forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
    assertThat(captor.getValue().getPasswordHash()).doesNotContain(PASSWORD);
  }

  @Test
  void should_sendVerificationEmail_when_registrationSucceeds() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));

    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendVerificationEmail(eq(EMAIL), linkCaptor.capture());
    assertThat(linkCaptor.getValue()).contains(BASE_URL + "/api/auth/verify?token=");
  }

  @Test
  void should_storeHashedTokenInDb_when_registering() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));

    ArgumentCaptor<User> userCaptor = forClass(User.class);
    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(userRepository).save(userCaptor.capture());
    verify(emailService).sendVerificationEmail(eq(EMAIL), linkCaptor.capture());

    String link = linkCaptor.getValue();
    String plainToken = link.substring(link.indexOf("token=") + 6);
    String storedToken = userCaptor.getValue().getVerificationToken();

    // DB stores SHA-256 hash, email link carries plain token
    assertThat(storedToken).isEqualTo(TokenHashUtils.hash(plainToken));
    assertThat(storedToken).isNotEqualTo(plainToken);
  }

  @Test
  void should_notSaveUser_when_emailAlreadyExists() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

    // Anti-enumeration: no exception thrown, caller cannot tell if email existed
    assertThatNoException()
        .isThrownBy(
            () -> authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT)));

    verify(userRepository, never()).save(any());
    verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
  }

  @Test
  void should_throwException_when_roleIsAdmin() {
    assertThatThrownBy(
            () -> authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.ADMIN)))
        .isInstanceOf(IllegalArgumentException.class);

    verify(userRepository, never()).findByEmail(anyString());
    verify(userRepository, never()).save(any());
  }

  @Test
  void should_lowercaseEmail_when_registering() {
    String upperEmail = "JAN@EXAMPLE.COM";
    when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    authService.register(new RegisterRequest(upperEmail, PASSWORD, UserRole.CREW));

    ArgumentCaptor<User> captor = forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getEmail()).isEqualTo("jan@example.com");
  }

  @Test
  void should_setTokenExpiresAt24hInFuture_when_registering() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    Instant before = Instant.now();
    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));
    Instant after = Instant.now();

    ArgumentCaptor<User> captor = forClass(User.class);
    verify(userRepository).save(captor.capture());
    Instant expiresAt = captor.getValue().getTokenExpiresAt();
    assertThat(expiresAt).isAfter(before.plusSeconds(23 * 3600));
    assertThat(expiresAt).isBefore(after.plusSeconds(25 * 3600));
  }

  @Test
  void should_generateUrlSafeToken_when_registering() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed");

    authService.register(new RegisterRequest(EMAIL, PASSWORD, UserRole.CLIENT));

    ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendVerificationEmail(eq(EMAIL), linkCaptor.capture());

    String link = linkCaptor.getValue();
    String plainToken = link.substring(link.indexOf("token=") + 6);
    assertThat(plainToken).matches("^[A-Za-z0-9_-]{64}$");
  }
}
