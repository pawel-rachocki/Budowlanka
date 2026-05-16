package com.budowlanka.backend.photo.entity;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "portfolio_photos")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_profile_id", nullable = false, updatable = false)
  private CrewProfile crewProfile;

  @Column(name = "storage_key", nullable = false, length = 512, updatable = false)
  private String storageKey;

  @Column(name = "thumbnail_key", length = 512)
  private String thumbnailKey;

  @Column(length = 255)
  private String caption;

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @Enumerated(EnumType.STRING)
  @Column(name = "moderation_status", nullable = false, length = 20)
  private ModerationStatus moderationStatus = ModerationStatus.PENDING;

  @Setter(AccessLevel.NONE)
  @Column(name = "moderation_note", columnDefinition = "TEXT")
  private String moderationNote;

  @Setter(AccessLevel.NONE)
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  @PrePersist
  protected void onCreate() {
    uploadedAt = Instant.now();
  }

  public void approve() {
    this.moderationStatus = ModerationStatus.APPROVED;
    this.moderationNote = null;
  }

  public void reject(String note) {
    this.moderationStatus = ModerationStatus.REJECTED;
    this.moderationNote = note;
  }
}
