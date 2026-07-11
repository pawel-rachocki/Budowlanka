package com.budowlanka.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budowlanka.backend.payment.dto.BoostPackageResponse;
import com.budowlanka.backend.payment.dto.ListingPackageResponse;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.ListingPackage;
import com.budowlanka.backend.payment.mapper.PackageMapper;
import com.budowlanka.backend.payment.repository.BoostPackageRepository;
import com.budowlanka.backend.payment.repository.ListingPackageRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

  @Mock private ListingPackageRepository listingPackageRepository;
  @Mock private BoostPackageRepository boostPackageRepository;
  @Mock private PackageMapper packageMapper;

  @InjectMocks private PackageService packageService;

  @Test
  void should_returnActiveListingPackagesMappedToResponse_when_called() {
    ListingPackage entity = mock(ListingPackage.class);
    ListingPackageResponse response =
        new ListingPackageResponse(UUID.randomUUID(), "7 dni", 7, new BigDecimal("29.00"));
    when(listingPackageRepository.findByActiveTrueOrderByPricePlnAsc()).thenReturn(List.of(entity));
    when(packageMapper.toResponse(entity)).thenReturn(response);

    List<ListingPackageResponse> result = packageService.getListingPackages();

    assertThat(result).containsExactly(response);
    verify(listingPackageRepository).findByActiveTrueOrderByPricePlnAsc();
  }

  @Test
  void should_returnEmptyList_when_noActiveListingPackages() {
    when(listingPackageRepository.findByActiveTrueOrderByPricePlnAsc()).thenReturn(List.of());

    List<ListingPackageResponse> result = packageService.getListingPackages();

    assertThat(result).isEmpty();
  }

  @Test
  void should_returnActiveBoostPackagesMappedToResponse_when_called() {
    BoostPackage entity = mock(BoostPackage.class);
    BoostPackageResponse response =
        new BoostPackageResponse(UUID.randomUUID(), "Boost 7 dni", 7, new BigDecimal("19.00"));
    when(boostPackageRepository.findByActiveTrueOrderByPricePlnAsc()).thenReturn(List.of(entity));
    when(packageMapper.toResponse(entity)).thenReturn(response);

    List<BoostPackageResponse> result = packageService.getBoostPackages();

    assertThat(result).containsExactly(response);
    verify(boostPackageRepository).findByActiveTrueOrderByPricePlnAsc();
  }

  @Test
  void should_returnEmptyList_when_noActiveBoostPackages() {
    when(boostPackageRepository.findByActiveTrueOrderByPricePlnAsc()).thenReturn(List.of());

    List<BoostPackageResponse> result = packageService.getBoostPackages();

    assertThat(result).isEmpty();
  }
}
