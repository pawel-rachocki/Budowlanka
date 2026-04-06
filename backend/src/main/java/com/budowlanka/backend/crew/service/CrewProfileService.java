package com.budowlanka.backend.crew.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.crew.dto.*;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.exception.CrewProfileAlreadyExistsException;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import com.budowlanka.backend.crew.specification.CrewProfileSpecification;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrewProfileService {

  private static final int MAX_SLUG_ATTEMPTS = 100;
  private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
  private static final Pattern EDGE_HYPHENS = Pattern.compile("^-|-$");
  private static final Map<Character, String> POLISH_CHARS = Map.of('ł', "l", 'Ł', "l");

  private final CrewProfileRepository crewProfileRepository;
  private final ServiceCategoryRepository serviceCategoryRepository;

  @Transactional
  public CrewProfileResponse createProfile(User user, CreateCrewProfileRequest req) {
    if (user.getRole() != UserRole.CREW) {
      throw new IllegalArgumentException("Tylko użytkownicy z rolą CREW mogą tworzyć profil.");
    }
    if (crewProfileRepository.existsByUserId(user.getId())) {
      throw new CrewProfileAlreadyExistsException();
    }

    String slug = generateUniqueSlug(req.companyName(), req.city(), null);
    Set<ServiceCategory> categories = resolveCategories(req.categoryIds());

    CrewProfile profile =
        CrewProfile.builder()
            .user(user)
            .companyName(req.companyName().trim())
            .slug(slug)
            .description(trimOrNull(req.description()))
            .phone(trimOrNull(req.phone()))
            .contactEmail(trimOrNull(req.contactEmail()))
            .city(req.city().trim())
            .voivodeship(req.voivodeship())
            .serviceRadiusKm(req.serviceRadiusKm() != null ? req.serviceRadiusKm() : 50)
            .nip(trimOrNull(req.nip()))
            .visible(true)
            .serviceCategories(categories)
            .build();

    crewProfileRepository.save(profile);
    log.info("Created crew profile slug={} for user={}", slug, user.getId());
    return toResponse(profile);
  }

  @Transactional
  public CrewProfileResponse updateProfile(User user, UpdateCrewProfileRequest req) {
    CrewProfile profile =
        crewProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(CrewProfileNotFoundException::new);

    boolean slugChanged = false;

    if (req.companyName() != null) {
      String trimmed = requireNonBlank(req.companyName(), "companyName");
      profile.setCompanyName(trimmed);
      slugChanged = true;
    }
    if (req.description() != null) {
      profile.setDescription(req.description().trim());
    }
    if (req.phone() != null) {
      profile.setPhone(req.phone().trim());
    }
    if (req.contactEmail() != null) {
      profile.setContactEmail(req.contactEmail().trim());
    }
    if (req.city() != null) {
      String trimmed = requireNonBlank(req.city(), "city");
      profile.setCity(trimmed);
      slugChanged = true;
    }
    if (req.voivodeship() != null) {
      profile.setVoivodeship(req.voivodeship());
    }
    if (req.serviceRadiusKm() != null) {
      profile.setServiceRadiusKm(req.serviceRadiusKm());
    }
    if (req.nip() != null) {
      profile.setNip(req.nip().trim());
    }
    if (req.categoryIds() != null) {
      Set<ServiceCategory> categories = resolveCategories(req.categoryIds());
      profile.getServiceCategories().clear();
      profile.getServiceCategories().addAll(categories);
    }

    if (slugChanged) {
      String newSlug =
          generateUniqueSlug(profile.getCompanyName(), profile.getCity(), profile.getId());
      profile.setSlug(newSlug);
    }

    crewProfileRepository.save(profile);
    log.info("Updated crew profile id={}", profile.getId());
    return toResponse(profile);
  }

  @Transactional(readOnly = true)
  public CrewProfileResponse getBySlug(String slug) {
    CrewProfile profile =
        crewProfileRepository.findBySlug(slug).orElseThrow(CrewProfileNotFoundException::new);
    return toResponse(profile);
  }

  @Transactional(readOnly = true)
  public CrewProfileResponse getMyProfile(User user) {
    CrewProfile profile =
        crewProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(CrewProfileNotFoundException::new);
    return toResponse(profile);
  }

  @Transactional(readOnly = true)
  public Page<CrewProfileSummaryResponse> search(
      String city, Voivodeship voivodeship, UUID categoryId, Pageable pageable) {
    Specification<CrewProfile> spec = Specification.where(CrewProfileSpecification.isVisible());

    if (city != null && !city.isBlank()) {
      spec = spec.and(CrewProfileSpecification.hasCity(city.trim()));
    }
    if (voivodeship != null) {
      spec = spec.and(CrewProfileSpecification.hasVoivodeship(voivodeship));
    }
    if (categoryId != null) {
      spec = spec.and(CrewProfileSpecification.hasCategory(categoryId));
    }

    return crewProfileRepository.findAll(spec, pageable).map(this::toSummaryResponse);
  }

  // --- private helpers ---

  private String generateUniqueSlug(String companyName, String city, UUID excludeId) {
    String base = slugify(companyName) + "-" + slugify(city);
    if (!slugExists(base, excludeId)) {
      return base;
    }
    for (int i = 2; i <= MAX_SLUG_ATTEMPTS; i++) {
      String candidate = base + "-" + i;
      if (!slugExists(candidate, excludeId)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Nie można wygenerować unikalnego sluga dla: " + base);
  }

  private boolean slugExists(String slug, UUID excludeId) {
    if (excludeId != null) {
      return crewProfileRepository.existsBySlugAndIdNot(slug, excludeId);
    }
    return crewProfileRepository.existsBySlug(slug);
  }

  static String slugify(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }
    StringBuilder sb = new StringBuilder(input.length());
    for (char c : input.toCharArray()) {
      String replacement = POLISH_CHARS.get(c);
      sb.append(replacement != null ? replacement : c);
    }
    String normalized = Normalizer.normalize(sb.toString(), Normalizer.Form.NFD);
    String stripped = normalized.replaceAll("\\p{M}", "");
    String slug = NON_ALPHANUMERIC.matcher(stripped.toLowerCase()).replaceAll("-");
    return EDGE_HYPHENS.matcher(slug).replaceAll("");
  }

  private Set<ServiceCategory> resolveCategories(Set<UUID> categoryIds) {
    if (categoryIds == null || categoryIds.isEmpty()) {
      return new HashSet<>();
    }
    List<ServiceCategory> found = serviceCategoryRepository.findAllById(categoryIds);
    if (found.size() != categoryIds.size()) {
      throw new IllegalArgumentException("Jedna lub więcej kategorii usług nie istnieje.");
    }
    return new HashSet<>(found);
  }

  private static String trimOrNull(String value) {
    return value != null ? value.trim() : null;
  }

  private String requireNonBlank(String value, String fieldName) {
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Pole " + fieldName + " nie może być puste.");
    }
    return trimmed;
  }

  private CrewProfileResponse toResponse(CrewProfile profile) {
    return new CrewProfileResponse(
        profile.getId(),
        profile.getCompanyName(),
        profile.getSlug(),
        profile.getDescription(),
        profile.getPhone(),
        profile.getContactEmail(),
        profile.getCity(),
        profile.getVoivodeship().getDisplayName(),
        profile.getServiceRadiusKm(),
        profile.getNip(),
        profile.getAvgRating(),
        profile.getReviewCount(),
        profile.isVisible(),
        mapCategories(profile.getServiceCategories()),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }

  private CrewProfileSummaryResponse toSummaryResponse(CrewProfile profile) {
    return new CrewProfileSummaryResponse(
        profile.getId(),
        profile.getCompanyName(),
        profile.getSlug(),
        profile.getCity(),
        profile.getVoivodeship().getDisplayName(),
        profile.getAvgRating(),
        profile.getReviewCount(),
        mapCategories(profile.getServiceCategories()));
  }

  private List<ServiceCategoryResponse> mapCategories(Set<ServiceCategory> categories) {
    return categories.stream()
        .map(c -> new ServiceCategoryResponse(c.getId(), c.getName(), c.getSlug()))
        .sorted(Comparator.comparing(ServiceCategoryResponse::name))
        .toList();
  }
}
