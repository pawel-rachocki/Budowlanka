package com.budowlanka.backend.auth.dto;

import com.budowlanka.backend.auth.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "musi zawierać co najmniej jedną literę, cyfrę i znak specjalny")
        String password,
    @NotNull UserRole role) {}
