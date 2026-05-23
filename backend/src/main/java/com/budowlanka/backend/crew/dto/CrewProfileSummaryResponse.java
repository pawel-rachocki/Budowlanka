package com.budowlanka.backend.crew.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CrewProfileSummaryResponse(
    UUID id,
    String companyName,
    String slug,
    String city,
    String voivodeship,
    BigDecimal avgRating,
    int reviewCount,
    List<ServiceCategoryResponse> serviceCategories,
    boolean boosted) {}
