package com.budowlanka.backend.photo.repository;

import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PortfolioPhotoRepository extends JpaRepository<PortfolioPhoto, UUID> {

  List<PortfolioPhoto> findByCrewProfileIdOrderByUploadedAtDesc(UUID crewId);

  List<PortfolioPhoto> findByCrewProfileIdAndModerationStatus(UUID crewId, ModerationStatus status);

  Page<PortfolioPhoto> findByModerationStatus(ModerationStatus status, Pageable pageable);

  @Query(
      value =
          "SELECT p FROM PortfolioPhoto p JOIN FETCH p.crewProfile WHERE p.moderationStatus ="
              + " :status",
      countQuery = "SELECT COUNT(p) FROM PortfolioPhoto p WHERE p.moderationStatus = :status")
  Page<PortfolioPhoto> findByModerationStatusWithCrew(
      @Param("status") ModerationStatus status, Pageable pageable);

  long countByCrewProfileId(UUID crewId);

  long countByModerationStatus(ModerationStatus status);
}
