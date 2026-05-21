package com.budowlanka.backend.review.dto;

import java.math.BigDecimal;

public record RatingStats(BigDecimal avgRating, Long reviewCount) {}
