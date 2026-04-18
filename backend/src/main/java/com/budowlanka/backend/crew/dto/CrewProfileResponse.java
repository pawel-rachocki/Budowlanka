package com.budowlanka.backend.crew.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CrewProfileResponse(
    UUID id,
    String companyName,
    String slug,
    String description,
    String phone,
    String contactEmail,
    String city,
    String voivodeship,
    Integer serviceRadiusKm,
    String nip,
    BigDecimal avgRating,
    int reviewCount,
    boolean visible,
    List<ServiceCategoryResponse> serviceCategories,
    Instant createdAt,
    Instant updatedAt) {}
