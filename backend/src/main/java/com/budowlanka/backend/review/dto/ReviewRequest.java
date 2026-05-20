package com.budowlanka.backend.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(min = 10, max = 1000)
        @Pattern(
            regexp = "^(?!\\s*$).*",
            message = "komentarz nie może składać się wyłącznie ze spacji")
        String comment) {}
