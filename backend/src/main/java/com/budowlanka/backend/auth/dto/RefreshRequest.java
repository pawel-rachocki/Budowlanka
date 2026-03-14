package com.budowlanka.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
    @NotBlank(message = "Refresh token jest wymagany.")
        @Size(max = 2048, message = "Token jest zbyt długi.")
        String refreshToken) {}
