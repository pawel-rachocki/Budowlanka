package com.budowlanka.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.rate-limit")
@Validated
public record RateLimitProperties(
    boolean enabled,
    @Valid @NotNull Limit login,
    @Valid @NotNull Limit register,
    @Valid @NotNull Limit reviews,
    @Valid @NotNull Limit profilesList) {

  public record Limit(@Positive long capacity, @NotNull Duration refillPeriod) {}
}
