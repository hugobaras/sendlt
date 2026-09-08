package com.sendlt.api.dto.attempt;

import com.sendlt.domain.enums.AttemptType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAttemptRequest(
        @NotNull UUID boulderId, @NotNull AttemptType type, @Min(1) int triesCount) {}
