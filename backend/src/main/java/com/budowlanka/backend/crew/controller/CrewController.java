package com.budowlanka.backend.crew.controller;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.common.PagedResponse;
import com.budowlanka.backend.crew.dto.CreateCrewProfileRequest;
import com.budowlanka.backend.crew.dto.CrewProfileResponse;
import com.budowlanka.backend.crew.dto.CrewProfileSummaryResponse;
import com.budowlanka.backend.crew.dto.UpdateCrewProfileRequest;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.service.CrewProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/crew/profiles")
@RequiredArgsConstructor
public class CrewController {

  private final CrewProfileService crewProfileService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('CREW')")
  public CrewProfileResponse create(
      @AuthenticationPrincipal User user, @Valid @RequestBody CreateCrewProfileRequest request) {
    return crewProfileService.createProfile(user, request);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('CREW')")
  public CrewProfileResponse getMine(@AuthenticationPrincipal User user) {
    return crewProfileService.getMyProfile(user);
  }

  @PutMapping("/me")
  @PreAuthorize("hasRole('CREW')")
  public CrewProfileResponse updateMine(
      @AuthenticationPrincipal User user, @Valid @RequestBody UpdateCrewProfileRequest request) {
    return crewProfileService.updateProfile(user, request);
  }

  /**
   * Zwraca profil po slugu. Dla zalogowanych użytkowników zawiera pełne dane kontaktowe; dla
   * anonimów pola phone i contactEmail są null. Ukryte profile są dostępne tylko dla ich
   * właściciela.
   */
  @GetMapping("/{slug}")
  public CrewProfileResponse getBySlug(
      @PathVariable @NotBlank @Size(max = 255) String slug, @AuthenticationPrincipal User user) {
    return crewProfileService.getBySlug(slug, user);
  }

  @GetMapping
  public PagedResponse<CrewProfileSummaryResponse> search(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) Voivodeship voivodeship,
      @RequestParam(required = false) UUID categoryId,
      @PageableDefault(size = 20) Pageable pageable) {
    return PagedResponse.from(crewProfileService.search(city, voivodeship, categoryId, pageable));
  }
}
