package com.budowlanka.backend.crew.repository;

import com.budowlanka.backend.crew.entity.CrewProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CrewProfileRepository
    extends JpaRepository<CrewProfile, UUID>, JpaSpecificationExecutor<CrewProfile> {

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findBySlug(String slug);

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findByUserId(UUID userId);

  Page<CrewProfile> findAll(Specification<CrewProfile> spec, Pageable pageable);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, UUID id);

  boolean existsByUserId(UUID userId);
}
