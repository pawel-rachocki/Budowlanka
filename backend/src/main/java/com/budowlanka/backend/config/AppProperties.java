package com.budowlanka.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public record AppProperties(@Valid JwtProperties jwt) {

  public record JwtProperties(
      @NotBlank @Size(min = 32) String secret,
      @Positive long accessTokenExpiration,
      @Positive long refreshTokenExpiration) {}
}
