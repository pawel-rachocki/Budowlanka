package com.budowlanka.backend.crew.repository;

import com.budowlanka.backend.crew.entity.CrewProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

  long countByVisibleTrue();

  /**
   * Ukrywa ({@code is_visible=false}) widoczne profile, które nie mają żadnej wciąż aktywnej
   * subskrypcji (rozumianej jako {@code expires_at > now} — zgodnie z decyzją arch. „profil
   * widoczny = aktywna subskrypcja z expires_at > NOW()"). Widoczność liczymy po {@code
   * expires_at}, nie po fladze {@code is_active}, więc wynik jest niezależny od kolejności względem
   * {@link com.budowlanka.backend.payment.repository.CrewSubscriptionRepository#deactivateExpired}.
   *
   * <p>Nigdy nie ustawia {@code visible=true}. Profile zablokowane mają już {@code
   * is_visible=false}, więc predykat {@code visible=true} je pomija — nie zostaną przypadkiem
   * odsłonięte. Idempotentne.
   *
   * @return liczba ukrytych profili
   */
  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE CrewProfile c SET c.visible = false "
          + "WHERE c.visible = true AND NOT EXISTS ("
          + "  SELECT 1 FROM CrewSubscription s "
          + "  WHERE s.crewProfile = c AND s.expiresAt > :now)")
  int hideProfilesWithoutActiveSubscription(@Param("now") Instant now);
}
