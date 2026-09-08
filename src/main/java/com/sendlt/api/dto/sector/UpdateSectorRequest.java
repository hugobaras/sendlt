package com.sendlt.api.dto.sector;

import jakarta.validation.constraints.NotBlank;

public record UpdateSectorRequest(@NotBlank String name) {}
