package com.budowlanka.backend.crew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.dto.CrewProfileSummaryResponse;
import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.mapper.CrewProfileMapper;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class CrewProfileServiceSearchTest {

  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private ServiceCategoryRepository serviceCategoryRepository;
  @Mock private CrewProfileMapper crewProfileMapper;

  @InjectMocks private CrewProfileService crewProfileService;

  private final Pageable defaultPageable = PageRequest.of(0, 20);

  @BeforeEach
  void setUp() {
    lenient()
        .when(crewProfileMapper.toSummaryResponse(any(CrewProfile.class)))
        .thenAnswer(
            inv -> {
              CrewProfile p = inv.getArgument(0);
              List<ServiceCategoryResponse> cats =
                  p.getServiceCategories().stream()
                      .map(c -> new ServiceCategoryResponse(c.getId(), c.getName(), c.getSlug()))
                      .sorted(Comparator.comparing(ServiceCategoryResponse::name))
                      .toList();
              return new CrewProfileSummaryResponse(
                  p.getId(),
                  p.getCompanyName(),
                  p.getSlug(),
                  p.getCity(),
                  p.getVoivodeship().name(),
                  p.getAvgRating(),
                  p.getReviewCount(),
                  cats,
                  p.isHasActiveBoost());
            });
  }

  @Test
  void should_returnVisibleProfiles_when_noFiltersApplied() {
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search(null, null, null, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_filterByCity_when_cityProvided() {
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search("Warszawa", null, null, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_filterByVoivodeship_when_voivodeshipProvided() {
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search(null, Voivodeship.MAZOWIECKIE, null, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_filterByCategoryId_when_categoryIdProvided() {
    UUID categoryId = UUID.randomUUID();
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search(null, null, categoryId, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_combineAllFilters_when_allProvided() {
    UUID categoryId = UUID.randomUUID();
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search("Kraków", Voivodeship.MALOPOLSKIE, categoryId, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_ignoreBlankCity_when_blankCityProvided() {
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search("   ", null, null, defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(crewProfileRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  void should_mapToSummaryResponse_when_resultsFound() {
    CrewProfile profile = buildProfile();
    Page<CrewProfile> page = new PageImpl<>(List.of(profile), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search(null, null, null, defaultPageable);

    CrewProfileSummaryResponse summary = result.getContent().getFirst();
    assertThat(summary.companyName()).isEqualTo("Test Remonty");
    assertThat(summary.slug()).isEqualTo("test-remonty-warszawa");
    assertThat(summary.city()).isEqualTo("Warszawa");
    assertThat(summary.voivodeship()).isEqualTo("MAZOWIECKIE");
    assertThat(summary.avgRating()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
    assertThat(summary.reviewCount()).isEqualTo(10);
    assertThat(summary.serviceCategories()).hasSize(1);
    assertThat(summary.serviceCategories().getFirst().name()).isEqualTo("Malowanie");
  }

  @Test
  void should_returnEmptyPage_when_noResults() {
    Page<CrewProfile> emptyPage = Page.empty(defaultPageable);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(emptyPage);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search("Nieistniejące", null, null, defaultPageable);

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  @Test
  void should_respectPagination_when_customPageableProvided() {
    Pageable customPageable = PageRequest.of(2, 5);
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), customPageable, 15);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    Page<CrewProfileSummaryResponse> result =
        crewProfileService.search(null, null, null, customPageable);

    assertThat(result.getNumber()).isEqualTo(2);
    assertThat(result.getSize()).isEqualTo(5);
    assertThat(result.getTotalElements()).isEqualTo(15);
    assertThat(result.getTotalPages()).isEqualTo(3);
    verify(crewProfileRepository)
        .findAll(
            any(Specification.class),
            argThat(
                (Pageable p) ->
                    p.getPageNumber() == 2 && p.getPageSize() == 5 && !p.getSort().isUnsorted()));
  }

  @Test
  void should_applyRankingSort_when_pageableIsUnsorted() {
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), defaultPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    crewProfileService.search(null, null, null, defaultPageable);

    verify(crewProfileRepository)
        .findAll(
            any(Specification.class),
            argThat(
                (Pageable p) -> {
                  List<Sort.Order> orders = p.getSort().toList();
                  return orders.size() == 3
                      && "hasActiveBoost".equals(orders.get(0).getProperty())
                      && orders.get(0).isDescending()
                      && "avgRating".equals(orders.get(1).getProperty())
                      && orders.get(1).isDescending()
                      && "reviewCount".equals(orders.get(2).getProperty())
                      && orders.get(2).isDescending();
                }));
  }

  @Test
  void should_preserveExplicitSort_when_sortProvided() {
    Sort customSort = Sort.by(Sort.Order.asc("avgRating"));
    Pageable sortedPageable = PageRequest.of(0, 20, customSort);
    Page<CrewProfile> page = new PageImpl<>(List.of(buildProfile()), sortedPageable, 1);
    when(crewProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(page);

    crewProfileService.search(null, null, null, sortedPageable);

    verify(crewProfileRepository)
        .findAll(any(Specification.class), argThat((Pageable p) -> p.getSort().equals(customSort)));
  }

  // --- helper ---

  private CrewProfile buildProfile() {
    ServiceCategory category =
        ServiceCategory.builder().name("Malowanie").slug("malowanie").build();

    CrewProfile profile =
        CrewProfile.builder()
            .user(User.builder().build())
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
