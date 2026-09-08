package com.sendlt.api.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record JoinSessionRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Z0-9]{6}$", message = "roomCode must be 6 uppercase alphanumeric characters")
        String roomCode) {}
