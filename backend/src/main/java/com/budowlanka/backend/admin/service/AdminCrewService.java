package com.budowlanka.backend.admin.service;

import com.budowlanka.backend.admin.dto.AdminCrewResponse;
import com.budowlanka.backend.admin.dto.BlockCrewRequest;
import com.budowlanka.backend.admin.mapper.AdminCrewMapper;
import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.crew.exception.CrewProfileNotFoundException;
import com.budowlanka.backend.crew.repository.CrewProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCrewService {

  private final CrewProfileRepository crewProfileRepository;
  private final AdminCrewMapper adminCrewMapper;

  @Transactional(readOnly = true)
  public Page<AdminCrewResponse> listCrews(Boolean blocked, Pageable pageable) {
    if (blocked != null) {
      return crewProfileRepository
          .findAllJoinUserByBlocked(blocked, pageable)
          .map(adminCrewMapper::toResponse);
    }
    return crewProfileRepository.findAllJoinUser(pageable).map(adminCrewMapper::toResponse);
  }

  @Transactional
  public AdminCrewResponse blockCrew(UUID id, BlockCrewRequest request) {
    CrewProfile profile =
        crewProfileRepository.findById(id).orElseThrow(CrewProfileNotFoundException::new);

    if (request.blocked()) {
      String reason = request.reason() != null ? request.reason().strip() : null;
      if (reason == null || reason.length() < 5) {
        throw new IllegalArgumentException("Powód blokady jest wymagany (min. 5 znaków).");
      }
      profile.block(reason);
      log.info("Admin blocked crew id={}", id);
    } else {
      profile.unblock();
      log.info("Admin unblocked crew id={}", id);
    }

    crewProfileRepository.save(profile);
    return adminCrewMapper.toResponse(profile);
  }
}
