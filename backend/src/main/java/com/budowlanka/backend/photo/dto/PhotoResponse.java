package com.budowlanka.backend.photo.dto;

import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
    UUID id,
    String storageKey,
    String thumbnailKey,
    String caption,
    ModerationStatus moderationStatus,
    String moderationNote,
    Instant uploadedAt) {

  public static PhotoResponse from(PortfolioPhoto photo) {
    return new PhotoResponse(
        photo.getId(),
        photo.getStorageKey(),
        photo.getThumbnailKey(),
        photo.getCaption(),
        photo.getModerationStatus(),
        photo.getModerationNote(),
        photo.getUploadedAt());
  }
}
