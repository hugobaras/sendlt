package com.sendlt.api.dto.gym;

import com.sendlt.domain.enums.ScoringMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateGymRequest(
        @NotBlank String name, String city, @NotNull ScoringMode scoringMode) {}
