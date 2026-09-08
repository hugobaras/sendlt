package com.sendlt.application.event;

import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.BoulderColor;
import java.time.Instant;
import java.util.UUID;

public record AttemptCreatedEvent(
        UUID attemptId,
        UUID sessionId,
        UUID boulderId,
        UUID userId,
        String userDisplayName,
        BoulderColor boulderColor,
        String gradeFont,
        String gradeVScale,
        AttemptType type,
        int triesCount,
        Instant createdAt) {}
