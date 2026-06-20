package com.budowlanka.backend.payment.repository;

import com.budowlanka.backend.payment.entity.ListingPackage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingPackageRepository extends JpaRepository<ListingPackage, UUID> {

  List<ListingPackage> findByActiveTrue();
}
