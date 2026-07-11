package com.budowlanka.backend.payment.controller;

import com.budowlanka.backend.payment.dto.BoostPackageResponse;
import com.budowlanka.backend.payment.dto.ListingPackageResponse;
import com.budowlanka.backend.payment.service.PackageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

  private final PackageService packageService;

  @GetMapping("/listing")
  public List<ListingPackageResponse> getListingPackages() {
    return packageService.getListingPackages();
  }

  @GetMapping("/boost")
  public List<BoostPackageResponse> getBoostPackages() {
    return packageService.getBoostPackages();
  }
}
