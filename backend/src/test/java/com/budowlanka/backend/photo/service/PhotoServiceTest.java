package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.exception.InvalidImageException;
import com.budowlanka.backend.photo.exception.PhotoLimitExceededException;
import com.budowlanka.backend.photo.exception.PhotoNotFoundException;
import com.budowlanka.backend.photo.exception.PhotoOwnershipException;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

  // Minimal JPEG magic bytes — imageValidator is mocked so content doesn't matter
  private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
  // Minimal PNG magic bytes (89 50 4E 47)
  private static final byte[] PNG_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x00};

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private PortfolioPhotoRepository photoRepository;
  @Mock private S3StorageService s3StorageService;
  @Mock private ImageValidator imageValidator;
  @Mock private ThumbnailService thumbnailService;
  @Mock private PhotoModerationService photoModerationService;

  private PhotoService service;

  @BeforeEach
  void setUp() {
    TransactionSynchronizationManager.initSynchronization();
    service =
        new PhotoService(
            crewProfileRepository,
            photoRepository,
            s3StorageService,
            imageValidator,
            thumbnailService,
            photoModerationService);
  }

  @AfterEach
  void tearDown() {
    TransactionSynchronizationManager.clearSynchronization();
  }

  /** Fires any afterCommit callbacks registered during the test (simulates TX commit). */
  private void triggerAfterCommit() {
    List<TransactionSynchronization> syncs =
        List.copyOf(TransactionSynchronizationManager.getSynchronizations());
    syncs.forEach(TransactionSynchronization::afterCommit);
  }

  // ── upload ───────────────────────────────────────────────────────────────

  @Test
  void should_returnPendingPhotoResponse_when_validUpload() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(0L);
    when(thumbnailService.generate(any())).thenReturn(new byte[] {1, 2, 3});
    when(s3StorageService.buildKey(eq(crewId), any()))
        .thenReturn("crew/id/original.jpg")
        .thenReturn("crew/id/thumb.jpg");
    when(photoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

    PhotoResponse response = service.upload(userId, file, "Kuchnia przed remontem");

    assertThat(response.moderationStatus()).isEqualTo(ModerationStatus.PENDING);
    assertThat(response.caption()).isEqualTo("Kuchnia przed remontem");
    verify(s3StorageService).uploadObject(JPEG_BYTES, "crew/id/original.jpg", "image/jpeg");
    verify(s3StorageService).uploadObject(any(), eq("crew/id/thumb.jpg"), eq("image/jpeg"));
    verify(photoModerationService, never()).moderateAsync(any());
    triggerAfterCommit();
    verify(photoModerationService).moderateAsync(any());
  }

  @Test
  void should_throwPhotoLimitExceededException_when_limitReached() {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(20L);

    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

    assertThatThrownBy(() -> service.upload(userId, file, null))
        .isInstanceOf(PhotoLimitExceededException.class)
        .hasMessageContaining("20");

    verify(imageValidator, never()).validate(any());
    verify(s3StorageService, never()).uploadObject(any(), anyString(), anyString());
  }

  @Test
  void should_throwInvalidImageException_when_fileInvalid() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(0L);
    doThrow(new InvalidImageException("Niedozwolony format pliku."))
        .when(imageValidator)
        .validate(any());

    MockMultipartFile file = new MockMultipartFile("file", "photo.txt", "text/plain", JPEG_BYTES);

    assertThatThrownBy(() -> service.upload(userId, file, null))
        .isInstanceOf(InvalidImageException.class);

    verify(s3StorageService, never()).uploadObject(any(), anyString(), anyString());
  }

  @Test
  void should_throwCrewProfileNotFoundException_when_userHasNoProfile() {
    UUID userId = UUID.randomUUID();
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

    assertThatThrownBy(() -> service.upload(userId, file, null))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  // ── listMine ─────────────────────────────────────────────────────────────

  @Test
  void should_returnAllPhotosWithModerationNote_when_listMine() {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));

    PortfolioPhoto approved =
        PortfolioPhoto.builder().crewProfile(crewProfile).storageKey("key-approved").build();
    approved.approve();

    PortfolioPhoto rejected =
        PortfolioPhoto.builder().crewProfile(crewProfile).storageKey("key-rejected").build();
    rejected.reject("Wykryto nieodpowiednie treści");

    when(photoRepository.findByCrewProfileIdOrderByUploadedAtDesc(crewId))
        .thenReturn(List.of(approved, rejected));

    List<PhotoResponse> result = service.listMine(userId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).moderationStatus()).isEqualTo(ModerationStatus.APPROVED);
    assertThat(result.get(1).moderationStatus()).isEqualTo(ModerationStatus.REJECTED);
    assertThat(result.get(1).moderationNote()).isEqualTo("Wykryto nieodpowiednie treści");
  }

  // ── listPublicBySlug ──────────────────────────────────────────────────────

  @Test
  void should_returnOnlyApprovedPhotos_when_listPublicBySlug() {
    String slug = "kowalski-remonty";
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findBySlug(slug)).thenReturn(Optional.of(crewProfile));

    PortfolioPhoto photo =
        PortfolioPhoto.builder().crewProfile(crewProfile).storageKey("key").build();
    photo.approve();

    when(photoRepository.findByCrewProfileIdAndModerationStatus(crewId, ModerationStatus.APPROVED))
        .thenReturn(List.of(photo));

    List<PhotoResponse> result = service.listPublicBySlug(slug);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().moderationStatus())
        .isNull(); // fromPublic hides moderation from public callers
  }

  // ── delete ───────────────────────────────────────────────────────────────

  @Test
  void should_deleteS3AndDb_when_ownerDeletes() {
    UUID photoId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);

    PortfolioPhoto photo =
        PortfolioPhoto.builder()
            .crewProfile(crewProfile)
            .storageKey("crew/test/original.jpg")
            .thumbnailKey("crew/test/thumb.jpg")
            .build();

    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));

    service.delete(photoId, userId);
    verify(photoRepository).delete(photo);

    triggerAfterCommit();
    verify(s3StorageService).deleteObject("crew/test/original.jpg");
    verify(s3StorageService).deleteObject("crew/test/thumb.jpg");
  }

  @Test
  void should_skipThumbnailS3Delete_when_thumbnailKeyIsNull() {
    UUID photoId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);

    PortfolioPhoto photo =
        PortfolioPhoto.builder()
            .crewProfile(crewProfile)
            .storageKey("crew/test/original.jpg")
            .build(); // thumbnailKey is null

    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));

    service.delete(photoId, userId);
    verify(photoRepository).delete(photo);

    triggerAfterCommit();
    verify(s3StorageService, times(1)).deleteObject(anyString());
    verify(s3StorageService).deleteObject("crew/test/original.jpg");
  }

  @Test
  void should_throwPhotoNotFoundException_when_photoDoesNotExist() {
    UUID photoId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(photoId, userId))
        .isInstanceOf(PhotoNotFoundException.class);

    verify(photoRepository, never()).delete(any());
  }

  @Test
  void should_throwPhotoOwnershipException_when_deletedByNonOwner() {
    UUID photoId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CrewProfile photoOwner = mock(CrewProfile.class);
    when(photoOwner.getId()).thenReturn(UUID.randomUUID());

    CrewProfile requester = mock(CrewProfile.class);
    when(requester.getId()).thenReturn(UUID.randomUUID());

    PortfolioPhoto photo =
        PortfolioPhoto.builder().crewProfile(photoOwner).storageKey("key").build();

    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(requester));

    assertThatThrownBy(() -> service.delete(photoId, userId))
        .isInstanceOf(PhotoOwnershipException.class);

    verify(s3StorageService, never()).deleteObject(anyString());
    verify(photoRepository, never()).delete(any());
  }

  // ── upload: content type detection (W1) ──────────────────────────────────

  @Test
  void should_usePngContentType_when_pngBytesUploaded() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(0L);
    when(thumbnailService.generate(any())).thenReturn(new byte[] {1});
    when(s3StorageService.buildKey(eq(crewId), any()))
        .thenReturn("crew/id/original.jpg")
        .thenReturn("crew/id/thumb.jpg");
    when(photoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", PNG_BYTES);
    service.upload(userId, file, null);

    verify(s3StorageService).uploadObject(PNG_BYTES, "crew/id/original.jpg", "image/png");
  }

  // ── upload: moderation timing (W2) ───────────────────────────────────────

  @Test
  void should_notCallModerateAsync_before_commit_when_upload() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(0L);
    when(thumbnailService.generate(any())).thenReturn(new byte[] {1});
    when(s3StorageService.buildKey(eq(crewId), any()))
        .thenReturn("crew/id/original.jpg")
        .thenReturn("crew/id/thumb.jpg");
    when(photoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);
    service.upload(userId, file, null);

    verify(photoModerationService, never()).moderateAsync(any());

    triggerAfterCommit();
    verify(photoModerationService).moderateAsync(any());
  }

  // ── upload: S3 partial rollback (W3) ─────────────────────────────────────

  @Test
  void should_rollbackOriginalS3Upload_when_thumbnailUploadFails() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    when(photoRepository.countByCrewProfileId(crewId)).thenReturn(0L);
    when(thumbnailService.generate(any())).thenReturn(new byte[] {1});
    when(s3StorageService.buildKey(eq(crewId), any()))
        .thenReturn("crew/id/original.jpg")
        .thenReturn("crew/id/thumb.jpg");
    when(s3StorageService.uploadObject(any(), anyString(), anyString()))
        .thenReturn("crew/id/original.jpg")
        .thenThrow(new RuntimeException("S3 unavailable"));

    MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

    assertThatThrownBy(() -> service.upload(userId, file, null))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("S3 unavailable");

    verify(s3StorageService).deleteObject("crew/id/original.jpg");
    verify(photoRepository, never()).save(any());
  }

  @Test
  void should_deleteFromDb_and_attemptS3_when_s3DeleteFails() {
    UUID photoId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID crewId = UUID.randomUUID();
    CrewProfile crewProfile = mock(CrewProfile.class);
    when(crewProfile.getId()).thenReturn(crewId);

    PortfolioPhoto photo =
        PortfolioPhoto.builder()
            .crewProfile(crewProfile)
            .storageKey("crew/test/original.jpg")
            .thumbnailKey("crew/test/thumb.jpg")
            .build();

    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));
    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(crewProfile));
    doThrow(new RuntimeException("S3 unavailable"))
        .when(s3StorageService)
        .deleteObject(anyString());

    service.delete(photoId, userId);
    verify(photoRepository).delete(photo); // DB delete already happened

    triggerAfterCommit(); // S3 calls fire — exceptions are swallowed by tryDeleteFromS3
    verify(s3StorageService).deleteObject("crew/test/original.jpg");
  }
}
