package com.budowlanka.backend.crew.controller;

import com.budowlanka.backend.crew.dto.ServiceCategoryResponse;
import com.budowlanka.backend.crew.service.ServiceCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ServiceCategoryController {

  private final ServiceCategoryService serviceCategoryService;

  @GetMapping
  public List<ServiceCategoryResponse> getAll() {
    return serviceCategoryService.getAll();
  }
}
