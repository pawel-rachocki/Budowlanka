package com.budowlanka.backend.crew.repository;

import com.budowlanka.backend.crew.entity.CrewProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CrewProfileRepository
    extends JpaRepository<CrewProfile, UUID>, JpaSpecificationExecutor<CrewProfile> {

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findBySlug(String slug);

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findByUserId(UUID userId);

  boolean existsBySlug(String slug);

  boolean existsByUserId(UUID userId);
}
