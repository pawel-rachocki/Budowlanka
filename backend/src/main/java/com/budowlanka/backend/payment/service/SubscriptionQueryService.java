package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.dto.SubscriptionStatusResponse;
import com.budowlanka.backend.payment.dto.SubscriptionStatusResponse.BoostInfo;
import com.budowlanka.backend.payment.dto.SubscriptionStatusResponse.SubscriptionInfo;
import com.budowlanka.backend.payment.repository.CrewBoostRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Odczyt statusu subskrypcji i boosta zalogowanej ekipy dla dashboardu (E-06). Read-only — nie
 * zmienia stanu aktywacji (tym zajmuje się {@link SubscriptionActivationService} po webhooku).
 *
 * <p>Za „aktywne" uznajemy subskrypcję z {@code is_active=true} i {@code expires_at > now} oraz
 * boost z {@code expires_at > now} — dokładnie tak, jak liczą to gotowe findery repozytoriów.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService {

  private final CrewProfileRepository crewProfileRepository;
  private final CrewSubscriptionRepository crewSubscriptionRepository;
  private final CrewBoostRepository crewBoostRepository;

  public SubscriptionStatusResponse getStatus(User user) {
    CrewProfile crew =
        crewProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(CrewProfileNotFoundException::new);

    Instant now = Instant.now();

    Optional<SubscriptionInfo> subscription =
        crewSubscriptionRepository
            .findFirstByCrewProfileIdAndActiveTrueAndExpiresAtAfterOrderByExpiresAtDesc(
                crew.getId(), now)
            .map(
                s ->
                    new SubscriptionInfo(
                        s.getListingPackage().getName(), s.getExpiresAt(), s.isActive()));

    BoostInfo boost =
        crewBoostRepository
            .findFirstByCrewProfileIdAndExpiresAtAfterOrderByExpiresAtDesc(crew.getId(), now)
            .map(b -> new BoostInfo(b.getBoostPackage().getName(), b.getExpiresAt()))
            .orElse(null);

    return new SubscriptionStatusResponse(
        subscription.isPresent(), crew.isVisible(), subscription.orElse(null), boost);
  }
}
