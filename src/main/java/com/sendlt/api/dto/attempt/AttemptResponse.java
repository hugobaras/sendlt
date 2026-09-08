package com.sendlt.api.dto.attempt;

import com.sendlt.domain.enums.AttemptType;
import java.time.Instant;
import java.util.UUID;

public record AttemptResponse(
        UUID id,
        UUID boulderId,
        UUID userId,
        UUID sessionId,
        AttemptType type,
        int triesCount,
        Instant createdAt) {}
