package com.budowlanka.backend.admin.dto;

import jakarta.validation.constraints.Size;

public record BlockCrewRequest(
    boolean blocked,
    @Size(min = 5, max = 500, message = "Powód blokady musi mieć od 5 do 500 znaków.")
        String reason) {}
