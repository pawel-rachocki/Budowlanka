package com.budowlanka.backend.photo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.config.SightEngineProperties;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.photo.client.ModerationScores;
import com.budowlanka.backend.photo.client.SightEngineClient;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.exception.ModerationApiException;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoModerationServiceTest {

  private static final SightEngineProperties ENABLED =
      new SightEngineProperties(true, "user", "secret", "https://api.sightengine.com");

  private static final SightEngineProperties DISABLED =
      new SightEngineProperties(false, "", "", "https://api.sightengine.com");

  @Mock private PortfolioPhotoRepository photoRepository;
  @Mock private S3StorageService s3StorageService;
  @Mock private SightEngineClient sightEngineClient;
  @Mock private CrewProfile crewProfile;

  private PhotoModerationService service;
  private UUID photoId;
  private PortfolioPhoto photo;

  @BeforeEach
  void setUp() {
    service =
        new PhotoModerationService(photoRepository, s3StorageService, sightEngineClient, ENABLED);
    photoId = UUID.randomUUID();
    photo =
        PortfolioPhoto.builder().crewProfile(crewProfile).storageKey("crew/test/photo.jpg").build();
    when(photoRepository.findById(photoId)).thenReturn(Optional.of(photo));
    lenient()
        .when(s3StorageService.publicUrl(anyString()))
        .thenReturn("https://cdn.example.com/photo.jpg");
  }

  @ParameterizedTest(name = "nudity={0} gore={1} weapon={2} → {3}")
  @MethodSource("thresholdCases")
  void should_apply_correct_moderation_status_when_scores_given(
      double nudity, double gore, double weapon, ModerationStatus expectedStatus) {
    when(sightEngineClient.check(anyString()))
        .thenReturn(new ModerationScores(nudity, gore, weapon, 0.0));

    service.moderateAsync(photoId);

    assertThat(photo.getModerationStatus()).isEqualTo(expectedStatus);
    verify(photoRepository).save(photo);
  }

  static Stream<Arguments> thresholdCases() {
    return Stream.of(
        Arguments.of(0.6, 0.0, 0.0, ModerationStatus.REJECTED),
        Arguments.of(0.0, 0.6, 0.0, ModerationStatus.REJECTED),
        Arguments.of(0.0, 0.0, 0.6, ModerationStatus.REJECTED),
        Arguments.of(0.51, 0.51, 0.0, ModerationStatus.REJECTED),
        // boundary: exactly 0.5 is NOT > 0.5 for REJECTED
        // nudity=0.5 → PENDING (0.5 > 0.2 nudity-warn applies)
        Arguments.of(0.5, 0.0, 0.0, ModerationStatus.PENDING),
        // gore/weapon=0.5 → APPROVED (no nudity-warn, no reject)
        Arguments.of(0.0, 0.5, 0.0, ModerationStatus.APPROVED),
        Arguments.of(0.0, 0.0, 0.5, ModerationStatus.APPROVED),
        Arguments.of(0.3, 0.0, 0.0, ModerationStatus.PENDING),
        Arguments.of(0.21, 0.0, 0.0, ModerationStatus.PENDING),
        // boundary: exactly 0.2 is NOT > 0.2, so → APPROVED
        Arguments.of(0.2, 0.0, 0.0, ModerationStatus.APPROVED),
        Arguments.of(0.1, 0.0, 0.0, ModerationStatus.APPROVED),
        Arguments.of(0.0, 0.0, 0.0, ModerationStatus.APPROVED));
  }

  @Test
  void should_set_rejection_note_when_rejected() {
    when(sightEngineClient.check(anyString())).thenReturn(new ModerationScores(0.9, 0.0, 0.0, 0.0));

    service.moderateAsync(photoId);

    assertThat(photo.getModerationStatus()).isEqualTo(ModerationStatus.REJECTED);
    assertThat(photo.getModerationNote()).isEqualTo("Wykryto nieodpowiednie treści");
  }

  @Test
  void should_keep_pending_status_and_not_save_when_api_throws() {
    when(sightEngineClient.check(anyString()))
        .thenThrow(new ModerationApiException("API failure", new RuntimeException()));

    service.moderateAsync(photoId);

    assertThat(photo.getModerationStatus()).isEqualTo(ModerationStatus.PENDING);
    verify(photoRepository, never()).save(any());
  }

  @Test
  void should_skip_when_photo_not_found() {
    when(photoRepository.findById(photoId)).thenReturn(Optional.empty());

    service.moderateAsync(photoId);

    verify(sightEngineClient, never()).check(anyString());
    verify(photoRepository, never()).save(any());
  }

  @Test
  void should_approve_immediately_and_skip_api_when_moderation_disabled() {
    service =
        new PhotoModerationService(photoRepository, s3StorageService, sightEngineClient, DISABLED);

    service.moderateAsync(photoId);

    assertThat(photo.getModerationStatus()).isEqualTo(ModerationStatus.APPROVED);
    verify(sightEngineClient, never()).check(anyString());
    verify(photoRepository).save(photo);
  }
}
