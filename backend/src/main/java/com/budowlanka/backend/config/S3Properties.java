package com.budowlanka.backend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.s3")
@Validated
public record S3Properties(
    @Pattern(regexp = "^$|https?://.+", message = "must be a valid http/https URL or empty")
        String endpoint,
    @NotBlank String region,
    @NotBlank String bucket,
    @NotBlank String accessKey,
    @NotBlank String secretKey,
    boolean forcePathStyle,
    String publicBaseUrl) {}
