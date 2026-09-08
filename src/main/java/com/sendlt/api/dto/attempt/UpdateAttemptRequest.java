package com.sendlt.api.dto.attempt;

import com.sendlt.domain.enums.AttemptType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateAttemptRequest(@NotNull AttemptType type, @Min(1) int triesCount) {}
