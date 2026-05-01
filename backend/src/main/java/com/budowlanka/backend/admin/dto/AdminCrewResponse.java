package com.budowlanka.backend.admin.dto;

import com.budowlanka.backend.crew.entity.CrewProfile;
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
    Instant createdAt) {

  public static AdminCrewResponse from(CrewProfile profile) {
    return new AdminCrewResponse(
        profile.getId(),
        profile.getCompanyName(),
        profile.getSlug(),
        profile.getCity(),
        profile.getVoivodeship().name(),
        profile.isVisible(),
        profile.isBlocked(),
        profile.getBlockReason(),
        profile.getAvgRating(),
        profile.getReviewCount(),
        profile.getUser().getEmail(),
        profile.getCreatedAt());
  }
}
