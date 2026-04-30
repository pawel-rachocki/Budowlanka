package com.budowlanka.backend.photo.dto;

import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.service.S3StorageService;
import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
    UUID id,
    String url,
    String thumbnailUrl,
    String caption,
    ModerationStatus moderationStatus,
    String moderationNote,
    Instant uploadedAt) {

  public static PhotoResponse fromOwner(PortfolioPhoto photo, S3StorageService s3) {
    return new PhotoResponse(
        photo.getId(),
        s3.publicUrl(photo.getStorageKey()),
        photo.getThumbnailKey() != null ? s3.publicUrl(photo.getThumbnailKey()) : null,
        photo.getCaption(),
        photo.getModerationStatus(),
        photo.getModerationNote(),
        photo.getUploadedAt());
  }

  public static PhotoResponse fromPublic(PortfolioPhoto photo, S3StorageService s3) {
    return new PhotoResponse(
        photo.getId(),
        s3.publicUrl(photo.getStorageKey()),
        photo.getThumbnailKey() != null ? s3.publicUrl(photo.getThumbnailKey()) : null,
        photo.getCaption(),
        null,
        null,
        photo.getUploadedAt());
  }
}
