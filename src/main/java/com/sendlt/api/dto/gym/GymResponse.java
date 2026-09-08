package com.sendlt.api.dto.gym;

import com.sendlt.domain.enums.ScoringMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record GymResponse(UUID id, String name, String city, ScoringMode scoringMode, Instant createdAt) {}
