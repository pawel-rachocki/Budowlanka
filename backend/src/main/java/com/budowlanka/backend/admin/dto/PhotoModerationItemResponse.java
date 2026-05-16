package com.budowlanka.backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record PhotoModerationItemResponse(
    UUID id,
    String originalUrl,
    String thumbnailUrl,
    String caption,
    String crewCompanyName,
    String crewSlug,
    Instant uploadedAt) {}
