package com.budowlanka.backend.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingPackageResponse(UUID id, String name, int durationDays, BigDecimal pricePln) {}
