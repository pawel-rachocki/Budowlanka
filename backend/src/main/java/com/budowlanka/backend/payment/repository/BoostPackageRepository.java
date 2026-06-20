package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.BoostPackage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoostPackageRepository extends JpaRepository<BoostPackage, UUID> {

  List<BoostPackage> findByActiveTrue();
}
