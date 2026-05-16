package com.budowlanka.backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.admin.dto.ModerationDecisionRequest;
import com.budowlanka.backend.admin.enums.ModerationDecision;
import com.budowlanka.backend.admin.exception.PhotoAlreadyDecidedException;
import com.budowlanka.backend.photo.dto.PhotoResponse;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import com.budowlanka.backend.photo.repository.PortfolioPhotoRepository;
import com.budowlanka.backend.photo.service.S3StorageService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceTest {

  @Mock private PortfolioPhotoRepository photoRepository;
  @Mock private S3StorageService s3StorageService;

  private AdminModerationService service;

  @BeforeEach
  void setUp() {
    service = new AdminModerationService(photoRepository, s3StorageService);
  }

  @Test
  void should_approvePhoto_when_decisionIsApprove() {
    PortfolioPhoto photo = pendingPhoto();
    when(photoRepository.findById(photo.getId())).thenReturn(Optional.of(photo));
    when(photoRepository.save(photo)).thenReturn(photo);
    when(s3StorageService.publicUrl(any())).thenReturn("https://cdn/img.jpg");

    PhotoResponse response =
        service.decide(
            photo.getId(), new ModerationDecisionRequest(ModerationDecision.APPROVE, null));

    assertThat(photo.getModerationStatus()).isEqualTo(ModerationStatus.APPROVED);
    assertThat(photo.getModerationNote()).isNull();
    assertThat(response.moderationStatus()).isEqualTo(ModerationStatus.APPROVED);
  }

  @Test
  void should_rejectPhoto_when_decisionIsRejectWithValidNote() {
    PortfolioPhoto photo = pendingPhoto();
    when(photoRepository.findById(photo.getId())).thenReturn(Optional.of(photo));
    when(photoRepository.save(photo)).thenReturn(photo);
    when(s3StorageService.publicUrl(any())).thenReturn("https://cdn/img.jpg");

    String note = "Zawiera nieodpowiednie treści";
    PhotoResponse response =
        service.decide(
            photo.getId(), new ModerationDecisionRequest(ModerationDecision.REJECT, note));

    assertThat(photo.getModerationStatus()).isEqualTo(ModerationStatus.REJECTED);
    assertThat(photo.getModerationNote()).isEqualTo(note);
    assertThat(response.moderationNote()).isEqualTo(note);
  }

  @Test
  void should_throwIllegalArgument_when_rejectWithoutNote() {
    PortfolioPhoto photo = pendingPhoto();
    when(photoRepository.findById(photo.getId())).thenReturn(Optional.of(photo));

    assertThatThrownBy(
            () ->
                service.decide(
                    photo.getId(), new ModerationDecisionRequest(ModerationDecision.REJECT, null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("min. 5 znaków");
  }

  @Test
  void should_throwIllegalArgument_when_rejectWithNoteTooShort() {
    PortfolioPhoto photo = pendingPhoto();
    when(photoRepository.findById(photo.getId())).thenReturn(Optional.of(photo));

    assertThatThrownBy(
            () ->
                service.decide(
                    photo.getId(), new ModerationDecisionRequest(ModerationDecision.REJECT, "ok")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("min. 5 znaków");
  }

  @Test
  void should_throwConflict_when_photoAlreadyDecided() {
    PortfolioPhoto photo = pendingPhoto();
    photo.approve();
    when(photoRepository.findById(photo.getId())).thenReturn(Optional.of(photo));

    assertThatThrownBy(
            () ->
                service.decide(
                    photo.getId(), new ModerationDecisionRequest(ModerationDecision.APPROVE, null)))
        .isInstanceOf(PhotoAlreadyDecidedException.class);
  }

  private PortfolioPhoto pendingPhoto() {
    return PortfolioPhoto.builder()
        .storageKey("crew/abc/img-original.jpg")
        .thumbnailKey("crew/abc/img-thumb.jpg")
        .caption("Test zdjęcie")
        .build();
  }
}
