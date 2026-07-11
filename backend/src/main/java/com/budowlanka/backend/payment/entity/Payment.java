package com.budowlanka.backend.payment.entity;

import com.budowlanka.backend.crew.entity.CrewProfile;
import com.budowlanka.backend.payment.enums.PaymentStatus;
import com.budowlanka.backend.payment.enums.PaymentType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "crew_profile_id", nullable = false, updatable = false)
  private CrewProfile crewProfile;

  @Column(name = "amount_pln", nullable = false, precision = 10, scale = 2)
  private BigDecimal amountPln;

  @Builder.Default
  @Column(nullable = false, length = 3)
  private String currency = "PLN";

  @Column(name = "payment_provider", nullable = false, length = 30)
  private String paymentProvider;

  @Column(name = "provider_tx_id", length = 255)
  private String providerTxId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type", nullable = false, length = 20)
  private PaymentType paymentType;

  @Column(name = "reference_id")
  private UUID referenceId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }

  /** Oznacza płatność jako zaksięgowaną (wywoływane z webhooka P24 — B7). */
  public void markCompleted(String providerTxId) {
    this.providerTxId = providerTxId;
    this.status = PaymentStatus.COMPLETED;
    this.completedAt = Instant.now();
  }

  public void markFailed() {
    this.status = PaymentStatus.FAILED;
  }

  /**
   * Przepina {@code reference_id} z identyfikatora pakietu (ustawianego przy inicjacji) na
   * utworzoną subskrypcję/boost po aktywacji płatności (B8 — REM-146).
   */
  public void linkActivatedResource(UUID resourceId) {
    this.referenceId = resourceId;
  }
}
