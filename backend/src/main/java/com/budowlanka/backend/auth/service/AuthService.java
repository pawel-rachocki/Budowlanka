package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.dto.RegisterRequest;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.exception.DuplicateEmailException;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.config.AppProperties;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private static final int TOKEN_BYTES = 48;
  private static final long TOKEN_VALIDITY_HOURS = 24;

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final AppProperties appProperties;

  public void register(RegisterRequest request) {
    if (request.role() == UserRole.ADMIN) {
      throw new IllegalArgumentException("Rola ADMIN nie może być wybrana podczas rejestracji.");
    }

    String email = request.email().toLowerCase(Locale.ROOT);

    if (userRepository.findByEmail(email).isPresent()) {
      throw new DuplicateEmailException(email);
    }

    String verificationToken = generateVerificationToken();

    User user =
        User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(request.role())
            .emailVerified(false)
            .verificationToken(verificationToken)
            .tokenExpiresAt(Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS))
            .build();

    userRepository.save(user);
    log.info("New user registered: {} (role={})", email, request.role());

    String verificationLink =
        appProperties.baseUrl() + "/api/auth/verify?token=" + verificationToken;
    emailService.sendVerificationEmail(email, verificationLink);
  }

  private String generateVerificationToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
