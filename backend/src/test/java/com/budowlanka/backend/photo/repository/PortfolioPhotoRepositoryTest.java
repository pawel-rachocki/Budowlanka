package com.budowlanka.backend.photo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import com.budowlanka.backend.auth.repository.UserRepository;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.enums.Voivodeship;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import com.budowlanka.backend.photo.entity.PortfolioPhoto;
import com.budowlanka.backend.photo.enums.ModerationStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PortfolioPhotoRepositoryTest {

  @Autowired private TestEntityManager em;
  @Autowired private PortfolioPhotoRepository photoRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CrewProfileRepository crewProfileRepository;

  @BeforeEach
  void cleanUp() {
    photoRepository.deleteAllInBatch();
    crewProfileRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  @Test
  void should_returnOnlyCrewPhotosNewestFirst_when_queryingByCrewProfileId()
      throws InterruptedException {
    CrewProfile crew1 = savedCrew("crew1@test.com", "crew-slug-1");
    CrewProfile crew2 = savedCrew("crew2@test.com", "crew-slug-2");

    PortfolioPhoto older = savedPhoto(crew1, ModerationStatus.PENDING);
    Thread.sleep(2); // ensure distinct uploadedAt timestamps
    PortfolioPhoto newer = savedPhoto(crew1, ModerationStatus.APPROVED);
    savedPhoto(crew2, ModerationStatus.PENDING);

    List<PortfolioPhoto> result =
        photoRepository.findByCrewProfileIdOrderByUploadedAtDesc(crew1.getId());

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(p -> p.getCrewProfile().getId().equals(crew1.getId()));
    assertThat(result.get(0).getId()).isEqualTo(newer.getId());
    assertThat(result.get(1).getId()).isEqualTo(older.getId());
  }

  @Test
  void should_returnOnlyApprovedPhotos_when_filteringByCrewProfileIdAndStatus() {
    CrewProfile crew = savedCrew("filter@test.com", "crew-slug-filter");

    savedPhoto(crew, ModerationStatus.APPROVED);
    savedPhoto(crew, ModerationStatus.APPROVED);
    savedPhoto(crew, ModerationStatus.REJECTED);
    savedPhoto(crew, ModerationStatus.PENDING);

    List<PortfolioPhoto> approved =
        photoRepository.findByCrewProfileIdAndModerationStatus(
            crew.getId(), ModerationStatus.APPROVED);

    assertThat(approved).hasSize(2);
    assertThat(approved).allMatch(p -> p.getModerationStatus() == ModerationStatus.APPROVED);
  }

  @Test
  void should_returnOnlyPendingPhotosWithPagination_when_queryingAdminModerationQueue() {
    CrewProfile crew = savedCrew("admin@test.com", "crew-slug-admin");

    savedPhoto(crew, ModerationStatus.PENDING);
    savedPhoto(crew, ModerationStatus.PENDING);
    savedPhoto(crew, ModerationStatus.APPROVED);

    Page<PortfolioPhoto> page =
        photoRepository.findByModerationStatus(ModerationStatus.PENDING, PageRequest.of(0, 10));

    assertThat(page.getContent())
        .allMatch(p -> p.getModerationStatus() == ModerationStatus.PENDING);
    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  void should_returnCorrectPhotoCount_when_countingByCrewProfileId() {
    CrewProfile crew1 = savedCrew("count1@test.com", "crew-slug-count1");
    CrewProfile crew2 = savedCrew("count2@test.com", "crew-slug-count2");

    savedPhoto(crew1, ModerationStatus.PENDING);
    savedPhoto(crew1, ModerationStatus.APPROVED);
    savedPhoto(crew1, ModerationStatus.REJECTED);
    savedPhoto(crew2, ModerationStatus.PENDING);

    assertThat(photoRepository.countByCrewProfileId(crew1.getId())).isEqualTo(3);
    assertThat(photoRepository.countByCrewProfileId(crew2.getId())).isEqualTo(1);
    assertThat(photoRepository.countByCrewProfileId(UUID.randomUUID())).isZero();
  }

  private CrewProfile savedCrew(String email, String slug) {
    User user =
        userRepository.save(
            User.builder()
                .email(email)
                .passwordHash("hash")
                .role(UserRole.CREW)
                .emailVerified(true)
                .build());

    return crewProfileRepository.save(
        CrewProfile.builder()
            .user(user)
            .companyName("Test Company")
            .slug(slug)
            .city("Warszawa")
            .voivodeship(Voivodeship.MAZOWIECKIE)
            .build());
  }

  private PortfolioPhoto savedPhoto(CrewProfile crew, ModerationStatus status) {
    PortfolioPhoto photo =
        PortfolioPhoto.builder()
            .crewProfile(crew)
            .storageKey("photos/" + UUID.randomUUID() + ".jpg")
            .build();
    em.persist(photo);
    if (status == ModerationStatus.APPROVED) photo.approve();
    else if (status == ModerationStatus.REJECTED) photo.reject("test rejection");
    em.flush();
    return photo;
  }
}
