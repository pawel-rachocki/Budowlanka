package com.budowlanka.backend.review.controller;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.common.PagedResponse;
import com.budowlanka.backend.review.dto.ReviewRequest;
import com.budowlanka.backend.review.dto.ReviewResponse;
import com.budowlanka.backend.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/crew/profiles/{slug}/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @GetMapping
  public PagedResponse<ReviewResponse> getReviews(
      @PathVariable @NotBlank @Size(max = 255) String slug,
      @PageableDefault(size = 20) Pageable pageable) {
    return PagedResponse.from(reviewService.getReviews(slug, pageable));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('CLIENT')")
  public ReviewResponse addReview(
      @PathVariable @NotBlank @Size(max = 255) String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody ReviewRequest request) {
    return reviewService.addReview(user, slug, request);
  }

  @PutMapping("/{reviewId}")
  @PreAuthorize("hasRole('CLIENT')")
  public ReviewResponse updateReview(
      @PathVariable @NotBlank @Size(max = 255) String slug,
      @PathVariable UUID reviewId,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody ReviewRequest request) {
    return reviewService.updateReview(user, slug, reviewId, request);
  }

  @DeleteMapping("/{reviewId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('CLIENT')")
  public void deleteReview(
      @PathVariable @NotBlank @Size(max = 255) String slug,
      @PathVariable UUID reviewId,
      @AuthenticationPrincipal User user) {
    reviewService.deleteReview(user, slug, reviewId);
  }
}
