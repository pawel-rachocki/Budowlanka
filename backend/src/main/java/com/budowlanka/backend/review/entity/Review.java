package com.budowlanka.backend.review.entity;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_reviews_crew_author",
            columnNames = {"crew_profile_id", "author_user_id"}))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_profile_id", nullable = false, updatable = false)
  private CrewProfile crewProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_user_id", nullable = false, updatable = false)
  private User author;

  @Column(nullable = false)
  private short rating;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  public void updateContent(short rating, String comment) {
    this.rating = rating;
    this.comment = comment;
  }
}
