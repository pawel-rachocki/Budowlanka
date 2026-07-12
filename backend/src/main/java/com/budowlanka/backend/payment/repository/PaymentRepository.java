package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.Payment;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

  /** Łączny przychód: suma zaksięgowanych płatności (COMPLETED). 0 gdy brak. */
  @Query(
      "SELECT COALESCE(SUM(p.amountPln), 0) FROM Payment p "
          + "WHERE p.status = com.budowlanka.backend.payment.enums.PaymentStatus.COMPLETED")
  BigDecimal sumCompletedAmountPln();

  /** Przychód od {@code since}: jak wyżej, ale tylko {@code completed_at >= since}. 0 gdy brak. */
  @Query(
      "SELECT COALESCE(SUM(p.amountPln), 0) FROM Payment p "
          + "WHERE p.status = com.budowlanka.backend.payment.enums.PaymentStatus.COMPLETED "
          + "AND p.completedAt >= :since")
  BigDecimal sumCompletedAmountPlnSince(@Param("since") Instant since);

  /** Projekcja wyniku {@link #sumCompletedAmountPlnByDaySince} — dzień i suma płatności. */
  interface DailyRevenue {
    LocalDate getDay();

    BigDecimal getAmount();
  }

  // Dzień liczony w strefie Europe/Warsaw — musi być zgodny ze strefą w AdminStatsService.
  // Dni bez płatności nie mają wiersza w wyniku — wołający dopełnia zerami.
  @Query(
      value =
          "SELECT (p.completed_at AT TIME ZONE 'Europe/Warsaw')::date AS day, "
              + "SUM(p.amount_pln) AS amount "
              + "FROM payments p "
              + "WHERE p.status = 'COMPLETED' AND p.completed_at >= :since "
              + "GROUP BY day ORDER BY day",
      nativeQuery = true)
  List<DailyRevenue> sumCompletedAmountPlnByDaySince(@Param("since") Instant since);
}
