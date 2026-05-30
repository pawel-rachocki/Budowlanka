package com.budowlanka.backend.crew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.crew.dto.CreateCrewProfileRequest;
import com.budowlanka.backend.crew.dto.CrewProfileResponse;
import com.budowlanka.backend.crew.dto.CrewProfileSummaryResponse;
import com.budowlanka.backend.crew.dto.UpdateCrewProfileRequest;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.exception.CrewProfileAlreadyExistsException;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.mapper.CrewProfileMapper;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CrewProfileServiceTest {

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private ServiceCategoryRepository serviceCategoryRepository;
  @Mock private CrewProfileMapper crewProfileMapper;

  @InjectMocks private CrewProfileService crewProfileService;

  private final Pageable defaultPageable = PageRequest.of(0, 20);

  @BeforeEach
  void setUp() {
    lenient()
        .when(crewProfileMapper.toResponse(any(CrewProfile.class)))
        .thenAnswer(
            inv -> {
              CrewProfile p = inv.getArgument(0);
              return new CrewProfileResponse(
                  p.getId(),
                  p.getCompanyName(),
                  p.getSlug(),
                  p.getDescription(),
                  p.getPhone(),
                  p.getContactEmail(),
                  p.getCity(),
                  p.getVoivodeship().name(),
                  p.getServiceRadiusKm(),
                  p.getNip(),
                  p.getAvgRating(),
                  p.getReviewCount(),
                  p.isVisible(),
                  List.of(),
                  p.getCreatedAt(),
                  p.getUpdatedAt());
            });
    lenient()
        .when(crewProfileMapper.toResponsePublic(any(CrewProfile.class)))
        .thenAnswer(
            inv -> {
              CrewProfile p = inv.getArgument(0);
              return new CrewProfileResponse(
                  p.getId(),
                  p.getCompanyName(),
                  p.getSlug(),
                  p.getDescription(),
                  null,
                  null,
                  p.getCity(),
                  p.getVoivodeship().name(),
                  p.getServiceRadiusKm(),
                  p.getNip(),
                  p.getAvgRating(),
                  p.getReviewCount(),
                  p.isVisible(),
                  List.of(),
                  p.getCreatedAt(),
                  p.getUpdatedAt());
            });
    lenient()
        .when(crewProfileMapper.toSummaryResponse(any(CrewProfile.class)))
        .thenAnswer(
            inv -> {
              CrewProfile p = inv.getArgument(0);
              return new CrewProfileSummaryResponse(
                  p.getId(),
                  p.getCompanyName(),
                  p.getSlug(),
                  p.getCity(),
                  p.getVoivodeship().name(),
                  p.getAvgRating(),
                  p.getReviewCount(),
                  List.of(),
                  p.isHasActiveBoost());
            });
  }

  // --- createProfile ---

  @Test
  void should_createProfile_when_validRequest() {
    UUID userId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);
    ServiceCategory category =
        ServiceCategory.builder().id(categoryId).name("Malowanie").slug("malowanie").build();

    when(crewProfileRepository.existsByUserId(userId)).thenReturn(false);
    when(crewProfileRepository.existsBySlug("test-remonty-warszawa")).thenReturn(false);
    when(serviceCategoryRepository.findAllById(Set.of(categoryId))).thenReturn(List.of(category));

    CreateCrewProfileRequest req =
        new CreateCrewProfileRequest(
            "Test Remonty",
            "Opis firmy",
            "600100200",
            "kontakt@test.pl",
            "Warszawa",
            Voivodeship.MAZOWIECKIE,
            null,
            "1234567890",
            Set.of(categoryId));

    CrewProfileResponse response = crewProfileService.createProfile(user, req);

    ArgumentCaptor<CrewProfile> captor = ArgumentCaptor.forClass(CrewProfile.class);
    verify(crewProfileRepository).save(captor.capture());
    CrewProfile saved = captor.getValue();

    assertThat(saved.getCompanyName()).isEqualTo("Test Remonty");
    assertThat(saved.getSlug()).isEqualTo("test-remonty-warszawa");
    assertThat(saved.getServiceRadiusKm()).isEqualTo(50);
    assertThat(saved.isVisible()).isTrue();
    assertThat(saved.getServiceCategories()).hasSize(1);
    assertThat(saved.getUser()).isEqualTo(user);

    assertThat(response.companyName()).isEqualTo("Test Remonty");
    assertThat(response.slug()).isEqualTo("test-remonty-warszawa");
  }

  @Test
  void should_generateUniqueSlug_when_collisionExists() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);

    when(crewProfileRepository.existsByUserId(userId)).thenReturn(false);
    when(crewProfileRepository.existsBySlug("test-remonty-warszawa")).thenReturn(true);
    when(crewProfileRepository.existsBySlug("test-remonty-warszawa-2")).thenReturn(false);
    CreateCrewProfileRequest req =
        new CreateCrewProfileRequest(
            "Test Remonty",
            null,
            null,
            null,
            "Warszawa",
            Voivodeship.MAZOWIECKIE,
            null,
            null,
            null);

    crewProfileService.createProfile(user, req);

    ArgumentCaptor<CrewProfile> captor = ArgumentCaptor.forClass(CrewProfile.class);
    verify(crewProfileRepository).save(captor.capture());

    assertThat(captor.getValue().getSlug()).isEqualTo("test-remonty-warszawa-2");
  }

  @Test
  void should_throwException_when_userAlreadyHasProfile() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);

    when(crewProfileRepository.existsByUserId(userId)).thenReturn(true);

    CreateCrewProfileRequest req =
        new CreateCrewProfileRequest(
            "Test Remonty",
            null,
            null,
            null,
            "Warszawa",
            Voivodeship.MAZOWIECKIE,
            null,
            null,
            null);

    assertThatThrownBy(() -> crewProfileService.createProfile(user, req))
        .isInstanceOf(CrewProfileAlreadyExistsException.class);

    verify(crewProfileRepository, never()).save(any());
  }

  @Test
  void should_throwAccessDenied_when_nonCrewUserCreatesProfile() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CLIENT);

    CreateCrewProfileRequest req =
        new CreateCrewProfileRequest(
            "Test Remonty",
            null,
            null,
            null,
            "Warszawa",
            Voivodeship.MAZOWIECKIE,
            null,
            null,
            null);

    assertThatThrownBy(() -> crewProfileService.createProfile(user, req))
        .isInstanceOf(AccessDeniedException.class);

    verify(crewProfileRepository, never()).save(any());
  }

  // --- updateProfile ---

  @Test
  void should_updateProfile_when_ownerEdits() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);
    CrewProfile profile = buildProfile(user);

    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

    UpdateCrewProfileRequest req =
        new UpdateCrewProfileRequest(
            null, "Nowy opis", "600999888", null, null, null, null, null, null);

    CrewProfileResponse response = crewProfileService.updateProfile(user, req);

    verify(crewProfileRepository).save(any(CrewProfile.class));

    assertThat(profile.getDescription()).isEqualTo("Nowy opis");
    assertThat(profile.getPhone()).isEqualTo("600999888");
    assertThat(profile.getCompanyName()).isEqualTo("Test Remonty");
    assertThat(profile.getSlug()).isEqualTo("test-remonty-warszawa");

    assertThat(response.description()).isEqualTo("Nowy opis");
  }

  // --- getBySlug ---

  @Test
  void should_returnProfile_when_slugExists() {
    UUID ownerId = UUID.randomUUID();
    UUID viewerId = UUID.randomUUID();
    User owner = buildUser(ownerId, UserRole.CREW);
    User viewer = buildUser(viewerId, UserRole.CLIENT);
    CrewProfile profile = buildProfile(owner);

    when(crewProfileRepository.findBySlug("test-remonty-warszawa"))
        .thenReturn(Optional.of(profile));

    CrewProfileResponse response = crewProfileService.getBySlug("test-remonty-warszawa", viewer);

    assertThat(response.companyName()).isEqualTo("Test Remonty");
    assertThat(response.slug()).isEqualTo("test-remonty-warszawa");
    assertThat(response.phone()).isEqualTo("600100200");
    assertThat(response.contactEmail()).isEqualTo("test@example.com");
    assertThat(response.city()).isEqualTo("Warszawa");
    assertThat(response.voivodeship()).isEqualTo("MAZOWIECKIE");
  }

  @Test
  void should_hideContactFields_when_anonymousViewer() {
    UUID ownerId = UUID.randomUUID();
    User owner = buildUser(ownerId, UserRole.CREW);
    CrewProfile profile = buildProfile(owner);

    when(crewProfileRepository.findBySlug("test-remonty-warszawa"))
        .thenReturn(Optional.of(profile));

    CrewProfileResponse response = crewProfileService.getBySlug("test-remonty-warszawa", null);

    assertThat(response.companyName()).isEqualTo("Test Remonty");
    assertThat(response.phone()).isNull();
    assertThat(response.contactEmail()).isNull();
  }

  @Test
  void should_throwNotFound_when_hiddenProfileViewedByNonOwner() {
    UUID ownerId = UUID.randomUUID();
    UUID viewerId = UUID.randomUUID();
    User owner = buildUser(ownerId, UserRole.CREW);
    User viewer = buildUser(viewerId, UserRole.CLIENT);
    CrewProfile profile = buildProfile(owner);
    profile.setVisible(false);

    when(crewProfileRepository.findBySlug("test-remonty-warszawa"))
        .thenReturn(Optional.of(profile));

    assertThatThrownBy(() -> crewProfileService.getBySlug("test-remonty-warszawa", viewer))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  @Test
  void should_throwNotFound_when_slugDoesNotExist() {
    when(crewProfileRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> crewProfileService.getBySlug("nonexistent", null))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  // --- getMyProfile ---

  @Test
  void should_returnMyProfile_when_profileExists() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);
    CrewProfile profile = buildProfile(user);

    when(crewProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

    CrewProfileResponse response = crewProfileService.getMyProfile(user);

    assertThat(response.companyName()).isEqualTo("Test Remonty");
    assertThat(response.phone()).isEqualTo("600100200");
    assertThat(response.contactEmail()).isEqualTo("test@example.com");
  }

  // --- search (smoke test) ---

  @Test
  void should_returnFilteredResults_when_searchWithFilters() {
    UUID userId = UUID.randomUUID();
    User user = buildUser(userId, UserRole.CREW);
    CrewProfile profile = buildProfile(user);
    UUID categoryId = UUID.randomUUID();

    Page<CrewProfile> page = new PageImpl<>(List.of(profile), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search("Warszawa", Voivodeship.MAZOWIECKIE, categoryId, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().companyName()).isEqualTo("Test Remonty");
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  // --- helpers ---

  private User buildUser(UUID id, UserRole role) {
    return User.builder()
        .id(id)
        .email("test@example.com")
        .passwordHash("hash")
        .role(role)
        .emailVerified(true)
        .build();
  }

  private CrewProfile buildProfile(User user) {
    ServiceCategory category =
        ServiceCategory.builder().name("Malowanie").slug("malowanie").build();

    CrewProfile profile =
        CrewProfile.builder()
            .id(UUID.randomUUID())
            .user(user)
            .companyName("Test Remonty")
            .slug("test-remonty-warszawa")
            .description("Opis firmy")
            .phone("600100200")
            .contactEmail("test@example.com")
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .serviceRadiusKm(50)
            .nip("1234567890")
            .visible(true)
            .serviceCategories(new HashSet<>(Set.of(category)))
            .build();
    profile.updateRatingStats(BigDecimal.valueOf(4.5), 10);
    return profile;
  }
}
