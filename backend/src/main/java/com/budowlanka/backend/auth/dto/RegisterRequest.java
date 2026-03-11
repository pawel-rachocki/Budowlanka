package com.budowlanka.backend.auth.dto;

import com.budowlanka.backend.auth.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotNull UserRole role) {}
