package com.budowlanka.backend.auth.repository;

import com.budowlanka.backend.auth.entity.RefreshToken;
import com.budowlanka.backend.auth.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findByUserAndRevokedFalse(User user);

  @Modifying
  @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
  void deleteByUser(@Param("user") User user);

  @Modifying
  @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
  void revokeAllActiveByUser(@Param("user") User user);
}
