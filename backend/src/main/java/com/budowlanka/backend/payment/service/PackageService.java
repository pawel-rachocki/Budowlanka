package com.budowlanka.backend.payment.service;

import com.budowlanka.backend.payment.dto.BoostPackageResponse;
import com.budowlanka.backend.payment.dto.ListingPackageResponse;
import com.budowlanka.backend.payment.mapper.PackageMapper;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

  private final ListingPackageRepository listingPackageRepository;
  private final BoostPackageRepository boostPackageRepository;
  private final PackageMapper packageMapper;

  @Cacheable("listingPackages")
  @Transactional(readOnly = true)
  public List<ListingPackageResponse> getListingPackages() {
    return listingPackageRepository.findByActiveTrueOrderByPricePlnAsc().stream()
        .map(packageMapper::toResponse)
        .toList();
  }

  @Cacheable("boostPackages")
  @Transactional(readOnly = true)
  public List<BoostPackageResponse> getBoostPackages() {
    return boostPackageRepository.findByActiveTrueOrderByPricePlnAsc().stream()
        .map(packageMapper::toResponse)
        .toList();
  }
}
