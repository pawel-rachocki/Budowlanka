package com.budowlanka.backend.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private CrewProfileRepository crewProfileRepository;
  @Mock private ReviewMapper reviewMapper;

  @InjectMocks private ReviewService reviewService;

  private final Pageable defaultPageable = PageRequest.of(0, 20);

  @BeforeEach
  void setUp() {
    lenient()
        .when(reviewMapper.toResponse(any(Review.class)))
        .thenAnswer(
            inv -> {
              Review r = inv.getArgument(0);
              String email = r.getAuthor().getEmail();
              String masked = email.substring(0, Math.min(3, email.indexOf('@'))) + "***";
              return new ReviewResponse(
                  r.getId(), r.getRating(), r.getComment(), masked, r.getCreatedAt());
            });
  }

  // --- addReview ---

  @Test
  void should_addReview_when_clientAndCrewExist() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    stubAddReviewSuccess(author, crew, 4);

    ReviewResponse response =
        reviewService.addReview(
            author, "kowalski-remonty", new ReviewRequest(4, "Świetna robota!"));

    assertThat(response).isNotNull();
    assertThat(response.rating()).isEqualTo(4);
    verify(reviewRepository).saveAndFlush(any(Review.class));
  }

  @Test
  void should_throw_DuplicateReviewException_when_reviewAlreadyExists() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");

    when(crewProfileRepository.findBySlug("kowalski-remonty")).thenReturn(Optional.of(crew));
    when(reviewRepository.existsByCrewProfileIdAndAuthorId(crew.getId(), author.getId()))
        .thenReturn(true);

    assertThatThrownBy(
            () ->
                reviewService.addReview(
                    author, "kowalski-remonty", new ReviewRequest(5, "Super ekipa!")))
        .isInstanceOf(DuplicateReviewException.class);

    verify(reviewRepository, never()).saveAndFlush(any());
  }

  @Test
  void should_throw_DuplicateReviewException_when_raceConditionOnSave() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");

    when(crewProfileRepository.findBySlug("kowalski-remonty")).thenReturn(Optional.of(crew));
    when(reviewRepository.existsByCrewProfileIdAndAuthorId(crew.getId(), author.getId()))
        .thenReturn(false);
    when(reviewRepository.saveAndFlush(any(Review.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key"));

    assertThatThrownBy(
            () ->
                reviewService.addReview(
                    author, "kowalski-remonty", new ReviewRequest(4, "Dobra robota!")))
        .isInstanceOf(DuplicateReviewException.class);
  }

  @Test
  void should_throw_IllegalArgumentException_when_crewTriesToReviewOwnProfile() {
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");

    when(crewProfileRepository.findBySlug("kowalski-remonty")).thenReturn(Optional.of(crew));

    assertThatThrownBy(
            () ->
                reviewService.addReview(
                    crewOwner, "kowalski-remonty", new ReviewRequest(5, "Polecam siebie!")))
        .isInstanceOf(IllegalArgumentException.class);

    verify(reviewRepository, never()).saveAndFlush(any());
  }

  @Test
  void should_recalculateAvgRating_when_reviewAdded() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    stubAddReviewSuccess(author, crew, 4);

    reviewService.addReview(author, "kowalski-remonty", new ReviewRequest(4, "Solidna ekipa."));

    verify(reviewRepository).calculateStats(crew.getId());
    assertThat(crew.getAvgRating()).isEqualByComparingTo(BigDecimal.valueOf(4.00));
    assertThat(crew.getReviewCount()).isEqualTo(1);
  }

  @Test
  void should_throw_CrewProfileNotFoundException_when_crewNotVisibleOnAdd() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "ukryta-ekipa");
    crew.setVisible(false);

    when(crewProfileRepository.findBySlug("ukryta-ekipa")).thenReturn(Optional.of(crew));

    assertThatThrownBy(
            () ->
                reviewService.addReview(
                    author, "ukryta-ekipa", new ReviewRequest(3, "Niewidoczna ekipa.")))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  // --- updateReview ---

  @Test
  void should_updateReview_when_authorIsOwner() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 3);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    when(reviewRepository.save(review)).thenReturn(review);
    stubRecalculate(crew, BigDecimal.valueOf(5.00), 1L);

    ReviewResponse response =
        reviewService.updateReview(
            author,
            "kowalski-remonty",
            reviewId,
            new ReviewRequest(5, "Po zastanowieniu — 5 gwiazdek."));

    assertThat(response).isNotNull();
    assertThat(response.rating()).isEqualTo(5);
    verify(reviewRepository).save(review);
  }

  @Test
  void should_throw_ReviewOwnershipException_when_notOwner_onUpdate() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User otherUser = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    assertThatThrownBy(
            () ->
                reviewService.updateReview(
                    otherUser, "kowalski-remonty", reviewId, new ReviewRequest(1, "Nie moja.")))
        .isInstanceOf(ReviewOwnershipException.class);

    verify(reviewRepository, never()).save(any());
  }

  @Test
  void should_throw_ReviewNotFoundException_when_reviewNotFound_onUpdate() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    UUID reviewId = UUID.randomUUID();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                reviewService.updateReview(
                    author, "kowalski-remonty", reviewId, new ReviewRequest(4, "Brak opinii.")))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  void should_throw_ReviewNotFoundException_when_slugMismatch_onUpdate() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "prawdziwy-slug");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    assertThatThrownBy(
            () ->
                reviewService.updateReview(
                    author, "inny-slug", reviewId, new ReviewRequest(4, "Zly slug.")))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  // --- deleteReview ---

  @Test
  void should_deleteReview_when_authorIsOwner() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    stubRecalculate(crew, BigDecimal.ZERO, 0L);

    reviewService.deleteReview(author, "kowalski-remonty", reviewId);

    verify(reviewRepository).delete(review);
    verify(reviewRepository).calculateStats(crew.getId());
  }

  @Test
  void should_throw_ReviewOwnershipException_when_notOwner_onDelete() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User otherUser = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.deleteReview(otherUser, "kowalski-remonty", reviewId))
        .isInstanceOf(ReviewOwnershipException.class);

    verify(reviewRepository, never()).delete(any());
  }

  @Test
  void should_throw_ReviewNotFoundException_when_reviewNotFound_onDelete() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    UUID reviewId = UUID.randomUUID();

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.deleteReview(author, "kowalski-remonty", reviewId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  void should_throw_ReviewNotFoundException_when_slugMismatch_onDelete() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "prawdziwy-slug");
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    assertThatThrownBy(() -> reviewService.deleteReview(author, "inny-slug", reviewId))
        .isInstanceOf(ReviewNotFoundException.class);
  }

  @Test
  void should_recalculateAvgRating_when_reviewDeleted() {
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    crew.updateRatingStats(BigDecimal.valueOf(4.00), 1);
    UUID reviewId = UUID.randomUUID();
    Review review = buildReview(crew, author, 4);

    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
    stubRecalculate(crew, null, 0L);

    reviewService.deleteReview(author, "kowalski-remonty", reviewId);

    assertThat(crew.getAvgRating()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(crew.getReviewCount()).isEqualTo(0);
  }

  // --- getReviews ---

  @Test
  void should_returnPagedReviews_when_crewExists() {
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "kowalski-remonty");
    User author = buildUser(UUID.randomUUID(), UserRole.CLIENT);
    Review review = buildReview(crew, author, 5);

    when(crewProfileRepository.findBySlug("kowalski-remonty")).thenReturn(Optional.of(crew));
    Page<Review> reviewPage = new PageImpl<>(List.of(review), defaultPageable, 1);
    when(reviewRepository.findByCrewProfileIdWithAuthor(crew.getId(), defaultPageable))
        .thenReturn(reviewPage);

    Page<ReviewResponse> result = reviewService.getReviews("kowalski-remonty", defaultPageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).hasSize(1);
    verify(reviewRepository).findByCrewProfileIdWithAuthor(crew.getId(), defaultPageable);
  }

  @Test
  void should_throw_CrewProfileNotFoundException_when_crewBlockedOnGetReviews() {
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "zablokowana-ekipa");
    crew.block("Naruszenie regulaminu");

    when(crewProfileRepository.findBySlug("zablokowana-ekipa")).thenReturn(Optional.of(crew));

    assertThatThrownBy(() -> reviewService.getReviews("zablokowana-ekipa", defaultPageable))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  @Test
  void should_throw_CrewProfileNotFoundException_when_crewNotVisibleOnGetReviews() {
    User crewOwner = buildUser(UUID.randomUUID(), UserRole.CREW);
    CrewProfile crew = buildCrewProfile(crewOwner, "niewidoczna-ekipa");
    crew.setVisible(false);

    when(crewProfileRepository.findBySlug("niewidoczna-ekipa")).thenReturn(Optional.of(crew));

    assertThatThrownBy(() -> reviewService.getReviews("niewidoczna-ekipa", defaultPageable))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  @Test
  void should_throw_CrewProfileNotFoundException_when_crewNotFoundOnGetReviews() {
    when(crewProfileRepository.findBySlug("nieistniejacy-slug")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reviewService.getReviews("nieistniejacy-slug", defaultPageable))
        .isInstanceOf(CrewProfileNotFoundException.class);
  }

  // --- helpers ---

  private User buildUser(UUID id, UserRole role) {
    return User.builder()
        .id(id)
        .email("user-" + id.toString().substring(0, 8) + "@example.com")
        .passwordHash("hash")
        .role(role)
        .emailVerified(true)
        .build();
  }

  private CrewProfile buildCrewProfile(User owner, String slug) {
    return CrewProfile.builder()
        .id(UUID.randomUUID())
        .user(owner)
        .companyName("Kowalski Remonty")
        .slug(slug)
        .city("Warszawa")
        .voivodeship(Voivodeship.MAZOWIECKIE)
        .visible(true)
        .build();
  }

  private Review buildReview(CrewProfile crew, User author, int rating) {
    return Review.builder()
        .id(UUID.randomUUID())
        .crewProfile(crew)
        .author(author)
        .rating((short) rating)
        .comment("Dobra robota, polecam!")
        .build();
  }

  private void stubRecalculate(CrewProfile crew, BigDecimal avg, Long count) {
    when(reviewRepository.calculateStats(crew.getId())).thenReturn(new RatingStats(avg, count));
    when(crewProfileRepository.findById(crew.getId())).thenReturn(Optional.of(crew));
  }

  private void stubAddReviewSuccess(User author, CrewProfile crew, int rating) {
    when(crewProfileRepository.findBySlug(crew.getSlug())).thenReturn(Optional.of(crew));
    when(reviewRepository.existsByCrewProfileIdAndAuthorId(crew.getId(), author.getId()))
        .thenReturn(false);
    when(reviewRepository.saveAndFlush(any(Review.class)))
        .thenReturn(buildReview(crew, author, rating));
    stubRecalculate(crew, BigDecimal.valueOf(rating), 1L);
  }
}
