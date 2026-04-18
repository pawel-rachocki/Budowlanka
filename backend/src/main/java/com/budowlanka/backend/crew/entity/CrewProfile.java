package com.budowlanka.backend.crew.entity;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.enums.Voivodeship;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "crew_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrewProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "company_name", nullable = false, length = 255)
  private String companyName;

  @Column(nullable = false, unique = true, length = 255)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 20)
  private String phone;

  @Column(name = "contact_email", length = 255)
  private String contactEmail;

  @Column(nullable = false, length = 100)
  private String city;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Voivodeship voivodeship;

  @Builder.Default
  @Column(name = "service_radius_km")
  private Integer serviceRadiusKm = 50;

  @Column(length = 10)
  private String nip;

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @Column(name = "avg_rating", precision = 3, scale = 2)
  private BigDecimal avgRating = BigDecimal.ZERO;

  @Builder.Default
  @Setter(AccessLevel.NONE)
  @Column(name = "review_count", nullable = false)
  private int reviewCount = 0;

  @Builder.Default
  @Column(name = "is_visible", nullable = false)
  private boolean visible = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "crew_services",
      joinColumns = @JoinColumn(name = "crew_profile_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  @BatchSize(size = 50)
  @Builder.Default
  private Set<ServiceCategory> serviceCategories = new HashSet<>();

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public void updateRatingStats(BigDecimal newAvgRating, int newReviewCount) {
    this.avgRating = newAvgRating;
    this.reviewCount = newReviewCount;
  }

  public void addServiceCategory(ServiceCategory category) {
    serviceCategories.add(category);
  }

  public void removeServiceCategory(ServiceCategory category) {
    serviceCategories.remove(category);
  }
}
