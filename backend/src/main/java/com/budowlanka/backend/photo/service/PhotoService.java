package com.budowlanka.backend.photo.service;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.enums.StorageKeySuffix;
import com.budowlanka.backend.photo.exception.PhotoLimitExceededException;
import com.budowlanka.backend.photo.exception.PhotoNotFoundException;
import com.budowlanka.backend.photo.exception.PhotoOwnershipException;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PhotoService {

  private static final int PHOTO_LIMIT = 20;

  private final CrewProfileRepository crewProfileRepository;
  private final PortfolioPhotoRepository photoRepository;
  private final S3StorageService s3StorageService;
  private final ImageValidator imageValidator;
  private final ThumbnailService thumbnailService;
  private final PhotoModerationService photoModerationService;

  public PhotoService(
      CrewProfileRepository crewProfileRepository,
      PortfolioPhotoRepository photoRepository,
      S3StorageService s3StorageService,
      ImageValidator imageValidator,
      ThumbnailService thumbnailService,
      PhotoModerationService photoModerationService) {
    this.crewProfileRepository = crewProfileRepository;
    this.photoRepository = photoRepository;
    this.s3StorageService = s3StorageService;
    this.imageValidator = imageValidator;
    this.thumbnailService = thumbnailService;
    this.photoModerationService = photoModerationService;
  }

  @Transactional
  public PhotoResponse upload(UUID userId, MultipartFile file, String caption) {
    CrewProfile crewProfile =
        crewProfileRepository.findByUserId(userId).orElseThrow(CrewProfileNotFoundException::new);

    // known TOCTOU — concurrent uploads may exceed limit by 1, acceptable for MVP
    if (photoRepository.countByCrewProfileId(crewProfile.getId()) >= PHOTO_LIMIT) {
      throw new PhotoLimitExceededException();
    }

    byte[] bytes = readBytes(file);
    imageValidator.validate(bytes);
    byte[] thumb = generateThumb(bytes);

    String originalKey = s3StorageService.buildKey(crewProfile.getId(), StorageKeySuffix.ORIGINAL);
    String thumbKey = s3StorageService.buildKey(crewProfile.getId(), StorageKeySuffix.THUMB);

    s3StorageService.uploadObject(bytes, originalKey, detectContentType(bytes));
    s3StorageService.uploadObject(thumb, thumbKey, "image/jpeg");

    PortfolioPhoto photo =
        PortfolioPhoto.builder()
            .crewProfile(crewProfile)
            .storageKey(originalKey)
            .thumbnailKey(thumbKey)
            .caption(caption)
            .build();

    photo = photoRepository.save(photo);
    photoModerationService.moderateAsync(photo.getId());

    return PhotoResponse.from(photo);
  }

  @Transactional(readOnly = true)
  public List<PhotoResponse> listMine(UUID userId) {
    CrewProfile crewProfile =
        crewProfileRepository.findByUserId(userId).orElseThrow(CrewProfileNotFoundException::new);
    return photoRepository.findByCrewProfileIdOrderByUploadedAtDesc(crewProfile.getId()).stream()
        .map(PhotoResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PhotoResponse> listPublicBySlug(String slug) {
    CrewProfile crewProfile =
        crewProfileRepository.findBySlug(slug).orElseThrow(CrewProfileNotFoundException::new);
    return photoRepository
        .findByCrewProfileIdAndModerationStatus(crewProfile.getId(), ModerationStatus.APPROVED)
        .stream()
        .map(PhotoResponse::from)
        .toList();
  }

  @Transactional
  public void delete(UUID photoId, UUID userId) {
    PortfolioPhoto photo =
        photoRepository.findById(photoId).orElseThrow(PhotoNotFoundException::new);
    CrewProfile crewProfile =
        crewProfileRepository.findByUserId(userId).orElseThrow(CrewProfileNotFoundException::new);

    if (!photo.getCrewProfile().getId().equals(crewProfile.getId())) {
      throw new PhotoOwnershipException();
    }

    String storageKey = photo.getStorageKey();
    String thumbnailKey = photo.getThumbnailKey();

    photoRepository.delete(photo);

    // S3 deletes run after DB commit to release the connection before network calls
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            tryDeleteFromS3(storageKey);
            if (thumbnailKey != null) {
              tryDeleteFromS3(thumbnailKey);
            }
          }
        });
  }

  private byte[] readBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new RuntimeException("Błąd odczytu pliku.", e);
    }
  }

  private byte[] generateThumb(byte[] bytes) {
    try {
      return thumbnailService.generate(bytes);
    } catch (IOException e) {
      throw new RuntimeException("Błąd generowania miniatury.", e);
    }
  }

  private void tryDeleteFromS3(String key) {
    try {
      s3StorageService.deleteObject(key);
    } catch (Exception e) {
      log.warn("S3 delete failed for key={}, file may need manual cleanup", key, e);
    }
  }

  private String detectContentType(byte[] bytes) {
    return (bytes[0] & 0xFF) == 0xFF ? "image/jpeg" : "image/png";
  }
}
