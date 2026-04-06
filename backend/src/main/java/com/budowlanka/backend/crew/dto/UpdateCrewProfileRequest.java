package com.budowlanka.backend.crew.dto;

import com.budowlanka.backend.crew.enums.Voivodeship;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateCrewProfileRequest(
    @Size(max = 255) String companyName,
    @Size(max = 2000) String description,
    @Size(max = 20) String phone,
    @Email @Size(max = 255) String contactEmail,
    @Size(max = 100) String city,
    Voivodeship voivodeship,
    @Positive Integer serviceRadiusKm,
    @Pattern(regexp = "\\d{10}", message = "NIP musi składać się z 10 cyfr") String nip,
    Set<UUID> categoryIds) {}
