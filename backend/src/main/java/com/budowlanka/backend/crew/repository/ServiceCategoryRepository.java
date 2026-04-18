package com.budowlanka.backend.crew.repository;

import com.budowlanka.backend.crew.entity.ServiceCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {

  Optional<ServiceCategory> findBySlug(String slug);

  Optional<ServiceCategory> findByName(String name);

  List<ServiceCategory> findAllByOrderByNameAsc();
}
