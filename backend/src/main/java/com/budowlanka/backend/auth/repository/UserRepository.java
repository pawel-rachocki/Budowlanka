package com.budowlanka.backend.auth.repository;

import com.budowlanka.backend.auth.entity.User;
import com.budowlanka.backend.auth.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  // NOTE: DB stores SHA-256 hash — call AuthService.hashToken(plainToken) before invoking this
  Optional<User> findByVerificationToken(String verificationToken);

  /** Projekcja wyniku {@link #countGroupedByRole()} — rola i liczba użytkowników. */
  interface RoleCount {
    UserRole getRole();

    long getCount();
  }

  // Role bez żadnego użytkownika nie mają wiersza w wyniku — wołający dopełnia zerami
  @Query("SELECT u.role AS role, COUNT(u) AS count FROM User u GROUP BY u.role")
  List<RoleCount> countGroupedByRole();
}
