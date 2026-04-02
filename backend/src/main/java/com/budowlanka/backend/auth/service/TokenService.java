package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.repository.RefreshTokenRepository;
import com.budowlanka.backend.auth.util.TokenHashUtils;
import com.budowlanka.backend.config.AppProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

  private static final String INVALID_TOKEN_MSG = "Sesja wygasła. Zaloguj się ponownie.";

  private final JwtService jwtService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AppProperties appProperties;

  @Transactional
  public IssuedTokens issueTokenPair(User user) {
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
    log.info("Token pair issued for user id={}", user.getId());
    return new IssuedTokens(accessToken, refreshJwt);
  }

  @Transactional
  public IssuedTokens refreshToken(String plainRefreshToken) {
    String hash = TokenHashUtils.hash(plainRefreshToken);
    RefreshToken stored =
        refreshTokenRepository
            .findByToken(hash)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG));

    if (stored.isRevoked()) {
      log.warn("Revoked refresh token reuse attempt for user id={}", stored.getUser().getId());
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG);
    }
    if (stored.getExpiresAt().isBefore(Instant.now())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG);
    }
    if (!jwtService.validateRefreshToken(plainRefreshToken)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG);
    }

    User user = stored.getUser();
    if (!user.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_TOKEN_MSG);
    }

    // Rotate: revoke the old token (kept for reuse detection), issue a new pair.
    // saveAndFlush forces the UPDATE to reach the DB before we INSERT the new token,
    // otherwise Hibernate batches both and the partial unique index fires (user_id WHERE
    // revoked=false).
    stored.revoke();
    refreshTokenRepository.saveAndFlush(stored);

    String newAccessToken = jwtService.generateAccessToken(user);
    String newRefreshJwt = jwtService.generateRefreshToken(user);
    refreshTokenRepository.save(
        RefreshToken.builder()
            .user(user)
            .token(TokenHashUtils.hash(newRefreshJwt))
            .expiresAt(Instant.now().plusMillis(appProperties.jwt().refreshTokenExpiration()))
            .build());

    log.info("Tokens rotated for user id={}", user.getId());
    return new IssuedTokens(newAccessToken, newRefreshJwt);
  }

  @Transactional
  public void logout(User user) {
    refreshTokenRepository.revokeAllActiveByUser(user);
    log.info("Refresh tokens revoked for user id={}", user.getId());
  }
}
