package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.dto.LoginRequest;
import com.budowlanka.backend.auth.dto.LoginResponse;
import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  // 48 bytes → 64 chars Base64URL (fits VARCHAR(128) with headroom)
  private static final int TOKEN_BYTES = 48;
  private static final long TOKEN_VALIDITY_HOURS = 24;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final AppProperties appProperties;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  @Transactional
  public LoginResponse login(LoginRequest request) {
    String email = request.email().toLowerCase(Locale.ROOT);
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password()));
    User user = (User) auth.getPrincipal();
    log.info("User logged in id={}", user.getId());
    return tokenService.issueTokenPair(user);
  }

  @Transactional
  public void verifyEmail(String plainToken) {
    String hashedToken = TokenHashUtils.hash(plainToken);
    User user =
        userRepository
            .findByVerificationToken(hashedToken)
            .orElseThrow(
                () -> new IllegalArgumentException("Token weryfikacyjny jest nieprawidłowy."));

    if (user.isEmailVerified()) return;

    if (user.getTokenExpiresAt() == null || Instant.now().isAfter(user.getTokenExpiresAt())) {
      throw new IllegalArgumentException("Token weryfikacyjny wygasł.");
    }

    user.setEmailVerified(true);
    user.setVerificationToken(null);
    user.setTokenExpiresAt(null);
    userRepository.save(user);
    log.info("Email verified for user id={}", user.getId());
  }

  @Transactional
  public void register(RegisterRequest request) {
    if (request.role() == UserRole.ADMIN) {
      throw new IllegalArgumentException("Rola ADMIN nie może być wybrana podczas rejestracji.");
    }

    String email = request.email().toLowerCase(Locale.ROOT);

    if (userRepository.findByEmail(email).isPresent()) {
      // Anti-enumeration: silently return — do not reveal whether email is already registered
      log.info("Registration attempt for already registered email");
      return;
    }

    String plainToken = generatePlainToken();

    User user =
        User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(request.role())
            .emailVerified(false)
            .verificationToken(
                TokenHashUtils.hash(plainToken)) // store SHA-256 hash, not plain token
            .tokenExpiresAt(Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS))
            .build();

    userRepository.save(user);
    log.info("New user registered (role={})", request.role());

    String verificationLink = appProperties.baseUrl() + "/api/auth/verify?token=" + plainToken;
    emailService.sendVerificationEmail(email, verificationLink);
  }

  private static String generatePlainToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
