package com.budowlanka.backend.auth.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.config.AppProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {

  private static final String ISSUER = "budowlanka-api";

  private final MACSigner signer;
  private final MACVerifier verifier;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtService(AppProperties props) {
    byte[] keyBytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
    try {
      this.signer = new MACSigner(keyBytes);
      this.verifier = new MACVerifier(keyBytes);
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT key too short — minimum 32 bytes required", e);
    }
    this.accessTokenExpiration = props.jwt().accessTokenExpiration();
    this.refreshTokenExpiration = props.jwt().refreshTokenExpiration();
  }

  public String generateAccessToken(User user) {
    return buildToken(user.getEmail(), accessTokenExpiration, "access");
  }

  public String generateRefreshToken(User user) {
    return buildToken(user.getEmail(), refreshTokenExpiration, "refresh");
  }

  /** Validates signature and expiry only — use {@link #validateAccessToken} in auth filter. */
  public boolean validateToken(String token) {
    return validateTokenWithType(token, null);
  }

  /** Validates signature, expiry, and that the token type is {@code access}. */
  public boolean validateAccessToken(String token) {
    return validateTokenWithType(token, "access");
  }

  public String extractUsername(String token) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      if (!jwt.verify(verifier)) {
        throw new IllegalArgumentException("Invalid token signature");
      }
      return jwt.getJWTClaimsSet().getSubject();
    } catch (ParseException | JOSEException e) {
      log.debug("Failed to extract username from token: {}", e.getMessage());
      throw new IllegalArgumentException("Invalid token", e);
    }
  }

  private boolean validateTokenWithType(String token, String expectedType) {
    try {
      SignedJWT jwt = SignedJWT.parse(token);
      if (!jwt.verify(verifier)) return false;
      JWTClaimsSet claims = jwt.getJWTClaimsSet();
      Date expiration = claims.getExpirationTime();
      if (expiration == null || !expiration.after(Date.from(Instant.now()))) return false;
      if (expectedType != null && !expectedType.equals(claims.getStringClaim("type"))) return false;
      return true;
    } catch (ParseException | JOSEException e) {
      log.debug("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  private String buildToken(String subject, long expirationMs, String type) {
    Instant now = Instant.now();
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(ISSUER)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusMillis(expirationMs)))
            .claim("type", type)
            .build();
    try {
      SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      jwt.sign(signer);
      return jwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign JWT", e);
    }
  }
}
