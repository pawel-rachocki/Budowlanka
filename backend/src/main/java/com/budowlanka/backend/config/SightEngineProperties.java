package com.budowlanka.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.moderation")
@Validated
public record SightEngineProperties(
    boolean enabled, String apiUser, String apiSecret, String baseUrl) {}
