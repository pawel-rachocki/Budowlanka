package com.budowlanka.backend.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.budowlanka.backend.IntegrationTestBase;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.service.SubscriptionExpiryService;
import com.budowlanka.backend.payment.service.SubscriptionExpiryService.ExpiryResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SubscriptionExpiryIntegrationTest extends IntegrationTestBase {

  @Autowired private SubscriptionExpiryService subscriptionExpiryService;
  @Autowired private CrewProfileRepository crewProfileRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User expiredUser;
  private User activeUser;
  private User blockedUser;
  private CrewProfile expiredCrew; // wygasła subskrypcja, widoczny → ma zniknąć
  private CrewProfile activeCrew; // aktywna subskrypcja, widoczny → zostaje
  private CrewProfile blockedCrew; // zablokowany, niewidoczny, wygasła subskrypcja → nietknięty
  private UUID packageId;

  @BeforeEach
  void setUp() {
    packageId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO listing_packages (id, name, duration_days, price_pln) VALUES (?, ?, ?, ?)",
        packageId,
        "30 dni",
        30,
        BigDecimal.valueOf(89.00));

    expiredUser = saveUser("expired-" + UUID.randomUUID() + "@test.com");
    activeUser = saveUser("active-" + UUID.randomUUID() + "@test.com");
    blockedUser = saveUser("blocked-" + UUID.randomUUID() + "@test.com");

    expiredCrew = saveCrewProfile(expiredUser, "Expired Crew", true, false);
    activeCrew = saveCrewProfile(activeUser, "Active Crew", true, false);
    blockedCrew = saveCrewProfile(blockedUser, "Blocked Crew", false, true);

    // wygasła (expires_at w przeszłości), flaga is_active=true
    insertSubscription(expiredCrew.getId(), "-2 days", true);
    // aktywna (expires_at w przyszłości)
    insertSubscription(activeCrew.getId(), "+10 days", true);
    // zablokowana ekipa — też ma wygasłą subskrypcję
    insertSubscription(blockedCrew.getId(), "-5 days", true);
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update(
        "DELETE FROM crew_subscriptions WHERE crew_profile_id IN (?, ?, ?)",
        expiredCrew.getId(),
        activeCrew.getId(),
        blockedCrew.getId());
    jdbcTemplate.update("DELETE FROM listing_packages WHERE id = ?", packageId);
    crewProfileRepository.deleteAll(List.of(expiredCrew, activeCrew, blockedCrew));
    userRepository.deleteAll(List.of(expiredUser, activeUser, blockedUser));
  }

  @Test
  void should_deactivateExpiredSubscription_and_hideProfile() {
    ExpiryResult result = subscriptionExpiryService.expireSubscriptions();

    assertThat(result.deactivatedSubscriptions()).isGreaterThanOrEqualTo(2); // expired + blocked
    assertThat(result.hiddenProfiles()).isGreaterThanOrEqualTo(1);

    assertThat(isSubscriptionActive(expiredCrew.getId())).isFalse();
    assertThat(isVisible(expiredCrew.getId())).isFalse();
  }

  @Test
  void should_keepActiveCrewVisible() {
    subscriptionExpiryService.expireSubscriptions();

    assertThat(isSubscriptionActive(activeCrew.getId())).isTrue();
    assertThat(isVisible(activeCrew.getId())).isTrue();
  }

  @Test
  void should_notResurrectBlockedCrew() {
    subscriptionExpiryService.expireSubscriptions();

    assertThat(isVisible(blockedCrew.getId())).isFalse();
    assertThat(isBlocked(blockedCrew.getId())).isTrue();
  }

  @Test
  void should_beIdempotent_onSecondRun() {
    subscriptionExpiryService.expireSubscriptions();
    ExpiryResult second = subscriptionExpiryService.expireSubscriptions();

    assertThat(second.deactivatedSubscriptions()).isZero();
    assertThat(second.hiddenProfiles()).isZero();
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
      User user, String companyName, boolean visible, boolean blocked) {
    String slug = companyName.toLowerCase().replace(" ", "-") + "-" + UUID.randomUUID();
    CrewProfile.CrewProfileBuilder builder =
        CrewProfile.builder()
            .user(user)
            .companyName(companyName)
            .slug(slug)
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .visible(visible)
            .blocked(blocked);
    if (blocked) {
      builder.blockReason("Test block");
    }
    return crewProfileRepository.save(builder.build());
  }

  private void insertSubscription(UUID crewId, String expiresInterval, boolean active) {
    jdbcTemplate.update(
        "INSERT INTO crew_subscriptions"
            + " (id, crew_profile_id, package_id, starts_at, expires_at, is_active, created_at)"
            + " VALUES (?, ?, ?, NOW() - INTERVAL '30 days', NOW() + INTERVAL '"
            + expiresInterval
            + "', ?, NOW())",
        UUID.randomUUID(),
        crewId,
        packageId,
        active);
  }

  private boolean isSubscriptionActive(UUID crewId) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT is_active FROM crew_subscriptions WHERE crew_profile_id = ?",
            Boolean.class,
            crewId));
  }

  private boolean isVisible(UUID crewId) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT is_visible FROM crew_profiles WHERE id = ?", Boolean.class, crewId));
  }

  private boolean isBlocked(UUID crewId) {
    return Boolean.TRUE.equals(
        jdbcTemplate.queryForObject(
            "SELECT blocked FROM crew_profiles WHERE id = ?", Boolean.class, crewId));
  }
}
