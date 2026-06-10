package com.sendlt.api.dto.sector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateSectorRequest(@NotNull UUID gymId, @NotBlank String name) {}
