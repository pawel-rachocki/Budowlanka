package com.budowlanka.backend.crew.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.crew.dto.*;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.entity.ServiceCategory;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.exception.BlankFieldException;
import com.budowlanka.backend.crew.exception.CrewProfileAlreadyExistsException;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.exception.ServiceCategoryNotFoundException;
import com.budowlanka.backend.crew.mapper.CrewProfileMapper;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import com.budowlanka.backend.crew.specification.CrewProfileSpecification;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
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
  private static final Set<String> RESERVED_SLUGS = Set.of("me", "admin", "api", "new");

  private static final Sort RANKING_SORT =
      Sort.by(
          Sort.Order.desc("hasActiveBoost"),
          Sort.Order.desc("avgRating"),
          Sort.Order.desc("reviewCount"));

  private final CrewProfileRepository crewProfileRepository;
  private final ServiceCategoryRepository serviceCategoryRepository;
  private final CrewProfileMapper crewProfileMapper;

  @Transactional
  public CrewProfileResponse createProfile(User user, CreateCrewProfileRequest req) {
    if (user.getRole() != UserRole.CREW) {
      throw new AccessDeniedException("Tylko użytkownicy z rolą CREW mogą tworzyć profil.");
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
    return crewProfileMapper.toResponse(profile);
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
    return crewProfileMapper.toResponse(profile);
  }

  /**
   * Zwraca profil po slugu z uwzględnieniem widoczności i kontekstu odwiedzającego.
   *
   * <p>Ukryte profile (np. z wygasłą subskrypcją) są dostępne tylko dla ich właściciela. Dane
   * kontaktowe (phone, contactEmail) są zwracane tylko zalogowanym użytkownikom — dla anonimów pola
   * te są null.
   *
   * @param slug unikalny slug profilu
   * @param viewer aktualnie zalogowany użytkownik (null dla anonima)
   * @throws CrewProfileNotFoundException jeśli profil nie istnieje lub jest ukryty dla tego
   *     odwiedzającego
   */
  @Transactional(readOnly = true)
  public CrewProfileResponse getBySlug(String slug, User viewer) {
    CrewProfile profile =
        crewProfileRepository.findBySlug(slug).orElseThrow(CrewProfileNotFoundException::new);

    boolean isOwner = viewer != null && profile.getUser().getId().equals(viewer.getId());
    if (!profile.isVisible() && !isOwner) {
      throw new CrewProfileNotFoundException();
    }

    return viewer != null
        ? crewProfileMapper.toResponse(profile)
        : crewProfileMapper.toResponsePublic(profile);
  }

  @Transactional(readOnly = true)
  public CrewProfileResponse getMyProfile(User user) {
    CrewProfile profile =
        crewProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(CrewProfileNotFoundException::new);
    return crewProfileMapper.toResponse(profile);
  }

  @Transactional(readOnly = true)
  public Page<CrewProfileSummaryResponse> search(
      String city, Voivodeship voivodeship, UUID categoryId, Pageable pageable) {
    Specification<CrewProfile> spec =
        Specification.where(CrewProfileSpecification.isVisible())
            .and(CrewProfileSpecification.isNotBlocked());

    if (city != null && !city.isBlank()) {
      spec = spec.and(CrewProfileSpecification.hasCity(city.trim()));
    }
    if (voivodeship != null) {
      spec = spec.and(CrewProfileSpecification.hasVoivodeship(voivodeship));
    }
    if (categoryId != null) {
      spec = spec.and(CrewProfileSpecification.hasCategory(categoryId));
    }

    Pageable ranked =
        pageable.getSort().isUnsorted()
            ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), RANKING_SORT)
            : pageable;

    return crewProfileRepository.findAll(spec, ranked).map(crewProfileMapper::toSummaryResponse);
  }

  // --- private helpers ---

  private String generateUniqueSlug(String companyName, String city, UUID excludeId) {
    String base = slugify(companyName) + "-" + slugify(city);
    if (isSlugAvailable(base, excludeId)) {
      return base;
    }
    for (int i = 2; i <= MAX_SLUG_ATTEMPTS; i++) {
      String candidate = base + "-" + i;
      if (isSlugAvailable(candidate, excludeId)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Nie można wygenerować unikalnego sluga dla: " + base);
  }

  private boolean isSlugAvailable(String slug, UUID excludeId) {
    if (RESERVED_SLUGS.contains(slug)) {
      return false;
    }
    if (excludeId != null) {
      return !crewProfileRepository.existsBySlugAndIdNot(slug, excludeId);
    }
    return !crewProfileRepository.existsBySlug(slug);
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
      throw new ServiceCategoryNotFoundException();
    }
    return new HashSet<>(found);
  }

  private static String trimOrNull(String value) {
    return value != null ? value.trim() : null;
  }

  private String requireNonBlank(String value, String fieldName) {
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new BlankFieldException(fieldName);
    }
    return trimmed;
  }
}
