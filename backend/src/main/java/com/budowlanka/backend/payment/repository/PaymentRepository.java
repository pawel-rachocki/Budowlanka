package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  Optional<Payment> findByProviderTxId(String providerTxId);

  List<Payment> findByCrewProfileIdOrderByCreatedAtDesc(UUID crewProfileId);

  @Query(
      value = "SELECT p FROM Payment p JOIN FETCH p.crewProfile",
      countQuery = "SELECT COUNT(p) FROM Payment p")
  Page<Payment> findAllJoinCrew(Pageable pageable);

  @Query(
      value = "SELECT p FROM Payment p JOIN FETCH p.crewProfile WHERE p.status = :status",
      countQuery = "SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
  Page<Payment> findAllJoinCrewByStatus(@Param("status") PaymentStatus status, Pageable pageable);
}
