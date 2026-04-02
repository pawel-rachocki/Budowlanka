package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.auth.dto.LoginRequest;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "Haslo123!";

  @Mock private UserRepository userRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private EmailService emailService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private TokenService tokenService;
  @Mock private Authentication authentication;

  private AuthService authService;
  private User verifiedUser;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(
                "test-secret-key-at-least-32-chars!!", 900_000L, 604_800_000L),
            "http://localhost:8080",
            true);
    authService =
        new AuthService(
            userRepository,
            passwordEncoder,
            emailService,
            props,
            authenticationManager,
            tokenService);

    verifiedUser =
        User.builder()
            .email(EMAIL)
            .passwordHash("hash")
            .role(UserRole.CLIENT)
            .emailVerified(true)
            .build();
  }

  @Test
  void should_returnIssuedTokens_when_validCredentials() {
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(verifiedUser);
    when(tokenService.issueTokenPair(verifiedUser))
        .thenReturn(new IssuedTokens("access-token", "refresh-token"));

    IssuedTokens result = authService.login(new LoginRequest(EMAIL, PASSWORD));

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.plainRefreshToken()).isEqualTo("refresh-token");
  }

  @Test
  void should_lowercaseEmail_when_authenticating() {
    String upperEmail = "TEST@EXAMPLE.COM";
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(verifiedUser);
    when(tokenService.issueTokenPair(any())).thenReturn(new IssuedTokens("a", "r"));

    authService.login(new LoginRequest(upperEmail, PASSWORD));

    ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
        forClass(UsernamePasswordAuthenticationToken.class);
    verify(authenticationManager).authenticate(captor.capture());
    assertThat(captor.getValue().getName()).isEqualTo("test@example.com");
  }

  @Test
  void should_propagateDisabledException_when_emailNotVerified() {
    when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

    assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
        .isInstanceOf(DisabledException.class);
  }

  @Test
  void should_propagateBadCredentialsException_when_wrongPassword() {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("bad credentials"));

    assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
        .isInstanceOf(BadCredentialsException.class);
  }
}
