package com.budowlanka.backend.admin.service;

import com.budowlanka.backend.admin.dto.ModerationDecisionRequest;
import com.budowlanka.backend.admin.dto.PhotoModerationItemResponse;
import com.budowlanka.backend.admin.enums.ModerationDecision;
import com.budowlanka.backend.admin.exception.PhotoAlreadyDecidedException;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.exception.PhotoNotFoundException;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import com.budowlanka.backend.photo.service.S3StorageService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AdminModerationService {

  private final PortfolioPhotoRepository photoRepository;
  private final S3StorageService s3StorageService;

  public AdminModerationService(
      PortfolioPhotoRepository photoRepository, S3StorageService s3StorageService) {
    this.photoRepository = photoRepository;
    this.s3StorageService = s3StorageService;
  }

  @Transactional(readOnly = true)
  public Page<PhotoModerationItemResponse> queue(ModerationStatus status, Pageable pageable) {
    return photoRepository
        .findByModerationStatusWithCrew(status, pageable)
        .map(
            photo ->
                new PhotoModerationItemResponse(
                    photo.getId(),
                    s3StorageService.publicUrl(photo.getStorageKey()),
                    photo.getThumbnailKey() != null
                        ? s3StorageService.publicUrl(photo.getThumbnailKey())
                        : null,
                    photo.getCaption(),
                    photo.getCrewProfile().getCompanyName(),
                    photo.getCrewProfile().getSlug(),
                    photo.getUploadedAt()));
  }

  @Transactional
  public PhotoResponse decide(UUID photoId, ModerationDecisionRequest request) {
    PortfolioPhoto photo =
        photoRepository.findById(photoId).orElseThrow(PhotoNotFoundException::new);

    if (photo.getModerationStatus() != ModerationStatus.PENDING) {
      throw new PhotoAlreadyDecidedException();
    }

    if (request.decision() == ModerationDecision.REJECT) {
      String trimmedNote = request.note() != null ? request.note().strip() : null;
      if (trimmedNote == null || trimmedNote.length() < 5) {
        throw new IllegalArgumentException(
            "Notatka jest wymagana przy odrzuceniu (min. 5 znaków).");
      }
      photo.reject(trimmedNote);
    } else {
      photo.approve();
    }

    photoRepository.save(photo);
    log.info("Admin decision={} for photoId={}", request.decision(), photoId);
    return PhotoResponse.fromOwner(photo, s3StorageService);
  }
}
