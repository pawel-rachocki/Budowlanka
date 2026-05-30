package com.budowlanka.backend.crew.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.crew.specification.CrewProfileSpecification;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class CrewRankingIntegrationTest extends IntegrationTestBase {

  private static final Sort RANKING_SORT =
      Sort.by(
          Sort.Order.desc("hasActiveBoost"),
          Sort.Order.desc("avgRating"),
          Sort.Order.desc("reviewCount"));

  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User boostedUser;
  private User topRatedUser;
  private User lowestRatedUser;
  private CrewProfile boostedCrew;
  private CrewProfile topRatedCrew;
  private CrewProfile lowestRatedCrew;
  private UUID boostPackageId;

  @BeforeEach
  void setUp() {
    boostedUser = saveUser("boosted-" + UUID.randomUUID() + "@test.com");
    topRatedUser = saveUser("top-rated-" + UUID.randomUUID() + "@test.com");
    lowestRatedUser = saveUser("lowest-" + UUID.randomUUID() + "@test.com");

    boostedCrew = saveCrewProfile(boostedUser, "Boosted Crew", BigDecimal.valueOf(3.00), 5);
    topRatedCrew = saveCrewProfile(topRatedUser, "Top Rated Crew", BigDecimal.valueOf(5.00), 20);
    lowestRatedCrew =
        saveCrewProfile(lowestRatedUser, "Lowest Rated Crew", BigDecimal.valueOf(2.00), 2);

    boostPackageId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO boost_packages (id, name, duration_days, price_pln) VALUES (?, ?, ?, ?)",
        boostPackageId,
        "Boost testowy",
        7,
        BigDecimal.valueOf(49.99));

    UUID boostId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO crew_boosts (id, crew_profile_id, boost_package_id, starts_at, expires_at)"
            + " VALUES (?, ?, ?, NOW(), NOW() + INTERVAL '7 days')",
        boostId,
        boostedCrew.getId(),
        boostPackageId);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update(
        "DELETE FROM crew_boosts WHERE crew_profile_id IN (?, ?, ?)",
        boostedCrew.getId(),
        topRatedCrew.getId(),
        lowestRatedCrew.getId());
    jdbcTemplate.update("DELETE FROM boost_packages WHERE id = ?", boostPackageId);
    crewProfileRepository.deleteAll(List.of(boostedCrew, topRatedCrew, lowestRatedCrew));
    userRepository.deleteAll(List.of(boostedUser, topRatedUser, lowestRatedUser));
  }

  @Test
  void should_sortBoostedCrewFirst_regardless_of_avgRating() {
    Pageable pageable = PageRequest.of(0, 10, RANKING_SORT);
    Specification<CrewProfile> spec =
        Specification.where(CrewProfileSpecification.isVisible())
            .and(CrewProfileSpecification.isNotBlocked());

    Page<CrewProfile> result = crewProfileRepository.findAll(spec, pageable);

    List<UUID> ids = result.getContent().stream().map(CrewProfile::getId).toList();
    assertThat(ids).contains(boostedCrew.getId(), topRatedCrew.getId(), lowestRatedCrew.getId());
    assertThat(ids.indexOf(boostedCrew.getId()))
        .as("Boosted crew should be first despite lower avg_rating (3.0 vs 5.0)")
        .isEqualTo(0);
  }

  @Test
  void should_sortByAvgRatingDesc_when_noBoostsActive() {
    jdbcTemplate.update("DELETE FROM crew_boosts WHERE crew_profile_id = ?", boostedCrew.getId());

    Pageable pageable = PageRequest.of(0, 10, RANKING_SORT);
    Specification<CrewProfile> spec =
        Specification.where(CrewProfileSpecification.isVisible())
            .and(CrewProfileSpecification.isNotBlocked());

    Page<CrewProfile> result = crewProfileRepository.findAll(spec, pageable);

    List<UUID> ids = result.getContent().stream().map(CrewProfile::getId).toList();
    int topRatedIdx = ids.indexOf(topRatedCrew.getId());
    int boostedIdx = ids.indexOf(boostedCrew.getId());
    int lowestIdx = ids.indexOf(lowestRatedCrew.getId());

    assertThat(topRatedIdx)
        .as("Top rated crew (5.0) should be first when no boosts")
        .isLessThan(boostedIdx);
    assertThat(boostedIdx)
        .as("Formerly-boosted crew (3.0) should be before lowest (2.0)")
        .isLessThan(lowestIdx);
  }

  // --- helpers ---

  private User saveUser(String email) {
    return userRepository.save(
        User.builder()
            .email(email)
            .passwordHash("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
            .role(UserRole.CREW)
            .emailVerified(true)
            .build());
  }

  private CrewProfile saveCrewProfile(
      User user, String companyName, BigDecimal avgRating, int reviewCount) {
    String slug = companyName.toLowerCase().replace(" ", "-") + "-" + UUID.randomUUID();
    CrewProfile profile =
        CrewProfile.builder()
            .user(user)
            .companyName(companyName)
            .slug(slug)
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .visible(true)
            .build();
    profile.updateRatingStats(avgRating, reviewCount);
    return crewProfileRepository.save(profile);
  }
}
