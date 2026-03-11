package com.budowlanka.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.config.AppProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "test-secret-key-at-least-32-chars!!";
  private static final long ACCESS_EXP = 900_000L;
  private static final long REFRESH_EXP = 604_800_000L;

  private JwtService jwtService;
  private User user;

  @BeforeEach
  void setUp() {
    AppProperties props =
        new AppProperties(
            new AppProperties.JwtProperties(SECRET, ACCESS_EXP, REFRESH_EXP),
            "http://localhost:8080");
    jwtService = new JwtService(props);

    user = new User();
    user.setEmail("test@example.com");
    user.setPasswordHash("hashed");
    user.setRole(UserRole.CLIENT);
  }

  @Test
  void should_generateNonNullToken_when_userProvided_accessToken() {
    assertThat(jwtService.generateAccessToken(user)).isNotBlank();
  }

  @Test
  void should_generateNonNullToken_when_userProvided_refreshToken() {
    assertThat(jwtService.generateRefreshToken(user)).isNotBlank();
  }

  @Test
  void should_returnTrue_when_validTokenProvided() {
    String token = jwtService.generateAccessToken(user);
    assertThat(jwtService.validateToken(token)).isTrue();
  }

  @Test
  void should_returnFalse_when_tokenExpired() throws Exception {
    Instant past = Instant.now().minusSeconds(3600);
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject(user.getEmail())
            .issuer("budowlanka-api")
            .issueTime(Date.from(past.minusSeconds(60)))
            .expirationTime(Date.from(past))
            .claim("type", "access")
            .build();
    SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));

    assertThat(jwtService.validateToken(jwt.serialize())).isFalse();
  }

  @Test
  void should_returnFalse_when_signatureIsInvalid() {
    JwtService otherService =
        new JwtService(
            new AppProperties(
                new AppProperties.JwtProperties(
                    "other-secret-key-at-least-32-chars!!", ACCESS_EXP, REFRESH_EXP),
                "http://localhost:8080"));
    assertThat(jwtService.validateToken(otherService.generateAccessToken(user))).isFalse();
  }

  @Test
  void should_returnFalse_when_tokenIsMalformed() {
    assertThat(jwtService.validateToken("not.a.jwt")).isFalse();
  }

  @Test
  void should_extractCorrectEmail_when_validTokenProvided() {
    String token = jwtService.generateAccessToken(user);
    assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
  }

  @Test
  void should_throwException_when_extractUsernameFromMalformedToken() {
    assertThatThrownBy(() -> jwtService.extractUsername("bad-token"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void should_throwException_when_extractUsernameFromTokenWithWrongSignature() {
    JwtService otherService =
        new JwtService(
            new AppProperties(
                new AppProperties.JwtProperties(
                    "other-secret-key-at-least-32-chars!!", ACCESS_EXP, REFRESH_EXP),
                "http://localhost:8080"));
    String forgedToken = otherService.generateAccessToken(user);
    assertThatThrownBy(() -> jwtService.extractUsername(forgedToken))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid token signature");
  }

  @Test
  void should_generateTokensWithDifferentExpiry_when_accessVsRefresh() throws ParseException {
    JWTClaimsSet accessClaims =
        SignedJWT.parse(jwtService.generateAccessToken(user)).getJWTClaimsSet();
    JWTClaimsSet refreshClaims =
        SignedJWT.parse(jwtService.generateRefreshToken(user)).getJWTClaimsSet();
    assertThat(refreshClaims.getExpirationTime()).isAfter(accessClaims.getExpirationTime());
  }

  @Test
  void should_embedCorrectType_when_tokenGenerated() throws ParseException {
    assertThat(
            SignedJWT.parse(jwtService.generateAccessToken(user))
                .getJWTClaimsSet()
                .getStringClaim("type"))
        .isEqualTo("access");
    assertThat(
            SignedJWT.parse(jwtService.generateRefreshToken(user))
                .getJWTClaimsSet()
                .getStringClaim("type"))
        .isEqualTo("refresh");
  }

  @Test
  void should_embedIssuer_when_tokenGenerated() throws ParseException {
    JWTClaimsSet claims = SignedJWT.parse(jwtService.generateAccessToken(user)).getJWTClaimsSet();
    assertThat(claims.getIssuer()).isEqualTo("budowlanka-api");
  }

  @Test
  void should_returnFalse_when_tokenHasWrongIssuer() throws Exception {
    Instant now = Instant.now();
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject(user.getEmail())
            .issuer("wrong-issuer")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(3600)))
            .claim("type", "access")
            .build();
    SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));

    assertThat(jwtService.validateToken(jwt.serialize())).isFalse();
  }

  @Test
  void should_returnTrue_when_validateAccessTokenWithAccessToken() {
    String token = jwtService.generateAccessToken(user);
    assertThat(jwtService.validateAccessToken(token)).isTrue();
  }

  @Test
  void should_returnFalse_when_validateAccessTokenWithRefreshToken() {
    String refreshToken = jwtService.generateRefreshToken(user);
    assertThat(jwtService.validateAccessToken(refreshToken)).isFalse();
  }
}
