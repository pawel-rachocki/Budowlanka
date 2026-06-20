package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

  Optional<Payment> findByProviderTxId(String providerTxId);

  List<Payment> findByCrewProfileIdOrderByCreatedAtDesc(UUID crewProfileId);
}
