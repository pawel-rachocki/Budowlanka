package com.budowlanka.backend.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    int rating,
    String comment,
    String authorDisplayName,
    UUID authorUserId,
    Instant createdAt) {}
