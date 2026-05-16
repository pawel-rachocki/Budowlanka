package com.budowlanka.backend.photo.service;

import com.budowlanka.backend.config.SightEngineProperties;
import com.budowlanka.backend.photo.client.ModerationScores;
import com.budowlanka.backend.photo.client.SightEngineClient;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PhotoModerationService {

  private final PortfolioPhotoRepository photoRepository;
  private final S3StorageService s3StorageService;
  private final SightEngineClient sightEngineClient;
  private final SightEngineProperties moderationProps;

  public PhotoModerationService(
      PortfolioPhotoRepository photoRepository,
      S3StorageService s3StorageService,
      SightEngineClient sightEngineClient,
      SightEngineProperties moderationProps) {
    this.photoRepository = photoRepository;
    this.s3StorageService = s3StorageService;
    this.sightEngineClient = sightEngineClient;
    this.moderationProps = moderationProps;
  }

  @Async("taskExecutor")
  @Transactional
  public void moderateAsync(UUID photoId) {
    Optional<PortfolioPhoto> opt = photoRepository.findById(photoId);
    if (opt.isEmpty()) {
      log.warn("Photo id={} not found during moderation, skipping", photoId);
      return;
    }
    PortfolioPhoto photo = opt.get();

    if (!moderationProps.enabled()) {
      photo.approve();
      photoRepository.save(photo);
      log.info("Moderation disabled — auto-approved photo id={}", photoId);
      return;
    }

    String publicUrl = s3StorageService.publicUrl(photo.getStorageKey());

    try {
      ModerationScores scores = sightEngineClient.check(publicUrl);
      applyDecision(photo, scores);
      photoRepository.save(photo);
    } catch (Exception e) {
      log.warn("Moderation API failed for photo id={}, status stays PENDING", photoId, e);
    }
  }

  private void applyDecision(PortfolioPhoto photo, ModerationScores scores) {
    if (scores.nudity() > 0.5 || scores.gore() > 0.5 || scores.weapon() > 0.5) {
      photo.reject("Wykryto nieodpowiednie treści");
      log.info(
          "Photo REJECTED id={} nudity={} gore={} weapon={}",
          photo.getId(),
          scores.nudity(),
          scores.gore(),
          scores.weapon());
    } else if (scores.nudity() > 0.2) {
      log.info(
          "Photo stays PENDING (manual review) id={} nudity={}", photo.getId(), scores.nudity());
    } else {
      photo.approve();
      log.info("Photo APPROVED id={}", photo.getId());
    }
  }
}
