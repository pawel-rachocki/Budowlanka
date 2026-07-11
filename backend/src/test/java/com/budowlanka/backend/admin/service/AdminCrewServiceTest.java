package com.budowlanka.backend.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.admin.dto.AdminCrewResponse;
import com.budowlanka.backend.admin.dto.BlockCrewRequest;
import com.budowlanka.backend.admin.mapper.AdminCrewMapper;
import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.payment.repository.CrewSubscriptionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCrewServiceTest {

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private CrewSubscriptionRepository crewSubscriptionRepository;
  @Mock private AdminCrewMapper adminCrewMapper;

  private AdminCrewService service;

  @BeforeEach
  void setUp() {
    service =
        new AdminCrewService(crewProfileRepository, crewSubscriptionRepository, adminCrewMapper);
    lenient()
        .when(adminCrewMapper.toResponse(any(CrewProfile.class)))
        .thenAnswer(
            inv -> {
              CrewProfile p = inv.getArgument(0);
              return new AdminCrewResponse(
                  p.getId(),
                  p.getCompanyName(),
                  p.getSlug(),
                  p.getCity(),
                  p.getVoivodeship() != null ? p.getVoivodeship().name() : null,
                  p.isVisible(),
                  p.isBlocked(),
                  p.getBlockReason(),
                  p.getAvgRating(),
                  p.getReviewCount(),
                  p.getUser() != null ? p.getUser().getEmail() : null,
                  p.getCreatedAt());
            });
  }

  @Test
  void should_blockCrew_when_validReasonProvided() {
    CrewProfile profile = crewProfile();
    when(crewProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
    when(crewProfileRepository.save(any())).thenReturn(profile);

    AdminCrewResponse response =
        service.blockCrew(profile.getId(), new BlockCrewRequest(true, "Naruszenie regulaminu"));

    assertThat(profile.isBlocked()).isTrue();
    assertThat(profile.isVisible()).isFalse();
    assertThat(profile.getBlockReason()).isEqualTo("Naruszenie regulaminu");
    assertThat(response.blocked()).isTrue();
    verify(crewProfileRepository).save(profile);
  }

  @Test
  void should_unblockCrew_and_makeVisible_when_activeSubscriptionExists() {
    CrewProfile profile = crewProfile();
    profile.block("Stary powód");
    when(crewProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
    when(crewProfileRepository.save(any())).thenReturn(profile);
    when(crewSubscriptionRepository.existsByCrewProfileIdAndActiveTrueAndExpiresAtAfter(
            eq(profile.getId()), any(Instant.class)))
        .thenReturn(true);

    AdminCrewResponse response =
        service.blockCrew(profile.getId(), new BlockCrewRequest(false, null));

    assertThat(profile.isBlocked()).isFalse();
    assertThat(profile.isVisible()).isTrue();
    assertThat(profile.getBlockReason()).isNull();
    assertThat(response.blocked()).isFalse();
    assertThat(response.visible()).isTrue();
    assertThat(response.blockReason()).isNull();
  }

  @Test
  void should_unblockCrew_and_stayHidden_when_noActiveSubscription() {
    CrewProfile profile = crewProfile();
    profile.block("Stary powód");
    when(crewProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));
    when(crewProfileRepository.save(any())).thenReturn(profile);
    when(crewSubscriptionRepository.existsByCrewProfileIdAndActiveTrueAndExpiresAtAfter(
            eq(profile.getId()), any(Instant.class)))
        .thenReturn(false);

    AdminCrewResponse response =
        service.blockCrew(profile.getId(), new BlockCrewRequest(false, null));

    assertThat(profile.isBlocked()).isFalse();
    assertThat(profile.isVisible()).isFalse();
    assertThat(profile.getBlockReason()).isNull();
    assertThat(response.blocked()).isFalse();
    assertThat(response.visible()).isFalse();
    assertThat(response.blockReason()).isNull();
  }

  @Test
  void should_throwIllegalArgument_when_blockWithoutReason() {
    CrewProfile profile = crewProfile();
    when(crewProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));

    assertThatThrownBy(() -> service.blockCrew(profile.getId(), new BlockCrewRequest(true, null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("min. 5 znaków");
  }

  @Test
  void should_throwIllegalArgument_when_blockWithTooShortReason() {
    CrewProfile profile = crewProfile();
    when(crewProfileRepository.findById(profile.getId())).thenReturn(Optional.of(profile));

    assertThatThrownBy(() -> service.blockCrew(profile.getId(), new BlockCrewRequest(true, "tak")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("min. 5 znaków");
  }

  @Test
  void should_throw404_when_crewNotFound() {
    UUID id = UUID.randomUUID();
    when(crewProfileRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.blockCrew(id, new BlockCrewRequest(true, "Naruszenie regulaminu")))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  private CrewProfile crewProfile() {
    User owner =
        User.builder()
            .email("ekipa@test.com")
            .passwordHash("hash")
            .role(UserRole.CREW)
            .emailVerified(true)
            .build();
    return CrewProfile.builder()
        .user(owner)
        .companyName("Test Ekipa")
        .slug("test-ekipa-warszawa")
        .city("Warszawa")
        .voivodeship(Voivodeship.MAZOWIECKIE)
        .visible(true)
        .build();
  }
}
