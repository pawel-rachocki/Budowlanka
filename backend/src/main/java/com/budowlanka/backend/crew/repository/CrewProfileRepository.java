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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewProfileRepository
    extends JpaRepository<CrewProfile, UUID>, JpaSpecificationExecutor<CrewProfile> {

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findBySlug(String slug);

  @EntityGraph(attributePaths = {"serviceCategories"})
  Optional<CrewProfile> findByUserId(UUID userId);

  Page<CrewProfile> findAll(Specification<CrewProfile> spec, Pageable pageable);

  @Query(
      value = "SELECT c FROM CrewProfile c JOIN FETCH c.user",
      countQuery = "SELECT COUNT(c) FROM CrewProfile c")
  Page<CrewProfile> findAllJoinUser(Pageable pageable);

  @Query(
      value = "SELECT c FROM CrewProfile c JOIN FETCH c.user WHERE c.blocked = :blocked",
      countQuery = "SELECT COUNT(c) FROM CrewProfile c WHERE c.blocked = :blocked")
  Page<CrewProfile> findAllJoinUserByBlocked(@Param("blocked") boolean blocked, Pageable pageable);

  boolean existsBySlug(String slug);

  boolean existsBySlugAndIdNot(String slug, UUID id);

  boolean existsByUserId(UUID userId);
}
