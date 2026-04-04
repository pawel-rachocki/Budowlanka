package com.budowlanka.backend.auth.repository;

import com.budowlanka.backend.auth.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  // NOTE: DB stores SHA-256 hash — call AuthService.hashToken(plainToken) before invoking this
  Optional<User> findByVerificationToken(String verificationToken);
}
