package com.budowlanka.backend.crew.service;

import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.repository.ServiceCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceCategoryService {
  private final ServiceCategoryRepository serviceCategoryRepository;

  @Cacheable("categories")
  @Transactional(readOnly = true)
  public List<ServiceCategoryResponse> getAll() {
    return serviceCategoryRepository.findAllByOrderByNameAsc().stream()
        .map(c -> new ServiceCategoryResponse(c.getId(), c.getName(), c.getSlug()))
        .toList();
  }
}
