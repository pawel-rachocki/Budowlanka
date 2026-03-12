package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.dto.LoginResponse;
import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

  private final JwtService jwtService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AppProperties appProperties;

  @Transactional
  public LoginResponse issueTokenPair(User user) {
    String accessToken = jwtService.generateAccessToken(user);
    String refreshJwt = jwtService.generateRefreshToken(user);
    Instant expiresAt = Instant.now().plusMillis(appProperties.jwt().refreshTokenExpiration());
    refreshTokenRepository.deleteByUser(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .token(TokenHashUtils.hash(refreshJwt))
            .expiresAt(expiresAt)
            .build());
    log.info("User logged in id={}", user.getId());
    return new LoginResponse(accessToken, refreshJwt, "Bearer");
  }
}
