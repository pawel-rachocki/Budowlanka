package com.budowlanka.backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminCrewResponse(
    UUID id,
    String companyName,
    String slug,
    String city,
    String voivodeship,
    boolean visible,
    boolean blocked,
    String blockReason,
    BigDecimal avgRating,
    int reviewCount,
    String ownerEmail,
    Instant createdAt) {}
