package com.sendlt.api.dto.sector;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record SectorResponse(UUID id, UUID gymId, String name, Instant createdAt) {}
