package com.budowlanka.backend.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BoostPackageResponse(UUID id, String name, int durationDays, BigDecimal pricePln) {}
