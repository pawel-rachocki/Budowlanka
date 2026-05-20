package com.budowlanka.backend.review.service;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.review.dto.RatingStats;
import com.budowlanka.backend.review.dto.ReviewRequest;
import com.budowlanka.backend.review.dto.ReviewResponse;
import com.budowlanka.backend.review.entity.Review;
import com.budowlanka.backend.review.exception.DuplicateReviewException;
import com.budowlanka.backend.review.exception.ReviewNotFoundException;
import com.budowlanka.backend.review.exception.ReviewOwnershipException;
import com.budowlanka.backend.review.mapper.ReviewMapper;
import com.budowlanka.backend.review.repository.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final CrewProfileRepository crewProfileRepository;
  private final ReviewMapper reviewMapper;

  public ReviewService(
      ReviewRepository reviewRepository,
      CrewProfileRepository crewProfileRepository,
      ReviewMapper reviewMapper) {
    this.reviewRepository = reviewRepository;
    this.crewProfileRepository = crewProfileRepository;
    this.reviewMapper = reviewMapper;
  }

  @Transactional
  public ReviewResponse addReview(User author, String crewSlug, ReviewRequest req) {
    CrewProfile crewProfile =
        crewProfileRepository.findBySlug(crewSlug).orElseThrow(CrewProfileNotFoundException::new);

    if (crewProfile.isBlocked() || !crewProfile.isVisible()) {
      throw new CrewProfileNotFoundException();
    }

    if (author.getId().equals(crewProfile.getUser().getId())) {
      throw new IllegalArgumentException("Nie możesz ocenić własnej ekipy.");
    }

    if (reviewRepository.existsByCrewProfileIdAndAuthorId(crewProfile.getId(), author.getId())) {
      throw new DuplicateReviewException();
    }

    Review review =
        Review.builder()
            .crewProfile(crewProfile)
            .author(author)
            .rating(req.rating().shortValue())
            .comment(req.comment())
            .build();

    try {
      Review saved = reviewRepository.saveAndFlush(review);
      recalculateRating(crewProfile.getId());
      return reviewMapper.toResponse(saved);
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateReviewException();
    }
  }

  @Transactional
  public ReviewResponse updateReview(
      User author, String crewSlug, UUID reviewId, ReviewRequest req) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

    if (!review.getCrewProfile().getSlug().equals(crewSlug)) {
      throw new ReviewNotFoundException();
    }

    if (!review.getAuthor().getId().equals(author.getId())) {
      throw new ReviewOwnershipException();
    }

    review.updateContent(req.rating().shortValue(), req.comment());

    Review saved = reviewRepository.save(review);
    recalculateRating(review.getCrewProfile().getId());
    return reviewMapper.toResponse(saved);
  }

  @Transactional
  public void deleteReview(User author, String crewSlug, UUID reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

    if (!review.getCrewProfile().getSlug().equals(crewSlug)) {
      throw new ReviewNotFoundException();
    }

    if (!review.getAuthor().getId().equals(author.getId())) {
      throw new ReviewOwnershipException();
    }

    UUID crewProfileId = review.getCrewProfile().getId();
    reviewRepository.delete(review);
    recalculateRating(crewProfileId);
  }

  @Transactional(readOnly = true)
  public Page<ReviewResponse> getReviews(String crewSlug, Pageable pageable) {
    CrewProfile crewProfile =
        crewProfileRepository.findBySlug(crewSlug).orElseThrow(CrewProfileNotFoundException::new);

    if (crewProfile.isBlocked() || !crewProfile.isVisible()) {
      throw new CrewProfileNotFoundException();
    }

    return reviewRepository
        .findByCrewProfileIdWithAuthor(crewProfile.getId(), pageable)
        .map(reviewMapper::toResponse);
  }

  private void recalculateRating(UUID crewProfileId) {
    RatingStats stats = reviewRepository.calculateStats(crewProfileId);
    BigDecimal avg =
        stats.avgRating() != null
            ? stats.avgRating().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    int count = stats.reviewCount() != null ? stats.reviewCount().intValue() : 0;

    CrewProfile profile = crewProfileRepository.findById(crewProfileId).orElseThrow();
    profile.updateRatingStats(avg, count);
  }
}
