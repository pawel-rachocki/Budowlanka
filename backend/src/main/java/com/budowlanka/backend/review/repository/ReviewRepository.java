package com.budowlanka.backend.review.repository;

import com.budowlanka.backend.review.dto.RatingStats;
import com.budowlanka.backend.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  Optional<Review> findByCrewProfileIdAndAuthorId(UUID crewProfileId, UUID authorId);

  boolean existsByCrewProfileIdAndAuthorId(UUID crewProfileId, UUID authorId);

  Page<Review> findByCrewProfileId(UUID crewProfileId, Pageable pageable);

  @Query(
      value = "SELECT r FROM Review r JOIN FETCH r.author WHERE r.crewProfile.id = :crewProfileId",
      countQuery = "SELECT COUNT(r) FROM Review r WHERE r.crewProfile.id = :crewProfileId")
  Page<Review> findByCrewProfileIdWithAuthor(
      @Param("crewProfileId") UUID crewProfileId, Pageable pageable);

  @Query(
      "SELECT new com.budowlanka.backend.review.dto.RatingStats(AVG(r.rating), COUNT(r))"
          + " FROM Review r WHERE r.crewProfile.id = :crewProfileId")
  RatingStats calculateStats(@Param("crewProfileId") UUID crewProfileId);
}
