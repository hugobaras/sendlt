package com.sendlt.api.dto.session;

import com.sendlt.domain.enums.SessionStatus;
import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String roomCode,
        SessionStatus status,
        UUID gymId,
        Instant createdAt,
        Instant closedAt) {}
