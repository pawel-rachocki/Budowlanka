package com.budowlanka.backend.payment.mapper;

import com.budowlanka.backend.payment.dto.BoostPackageResponse;
import com.budowlanka.backend.payment.dto.ListingPackageResponse;
import com.budowlanka.backend.payment.entity.BoostPackage;
import com.budowlanka.backend.payment.entity.ListingPackage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PackageMapper {

  ListingPackageResponse toResponse(ListingPackage listingPackage);

  BoostPackageResponse toResponse(BoostPackage boostPackage);
}
