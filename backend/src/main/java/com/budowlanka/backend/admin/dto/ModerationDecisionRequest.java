package com.budowlanka.backend.admin.dto;

import com.budowlanka.backend.admin.enums.ModerationDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerationDecisionRequest(
    @NotNull(message = "Decyzja jest wymagana.") ModerationDecision decision,
    @Size(max = 500, message = "Notatka może mieć maksymalnie 500 znaków.") String note) {}
