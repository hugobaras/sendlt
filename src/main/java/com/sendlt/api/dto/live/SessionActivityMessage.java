package com.sendlt.api.dto.live;

import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.BoulderColor;
import java.time.Instant;
import java.util.UUID;

public record SessionActivityMessage(
        String eventType,
        UUID sessionId,
        Instant timestamp,
        AttemptPayload attempt,
        ParticipantPayload participant) {

    public static SessionActivityMessage attemptCreated(
            UUID sessionId,
            UUID attemptId,
            UUID userId,
            String userDisplayName,
            UUID boulderId,
            BoulderColor boulderColor,
            String gradeFont,
            String gradeVScale,
            AttemptType type,
            int triesCount,
            Instant createdAt) {
        return new SessionActivityMessage(
                "ATTEMPT_CREATED",
                sessionId,
                createdAt,
                new AttemptPayload(
                        attemptId,
                        userId,
                        userDisplayName,
                        boulderId,
                        boulderColor,
                        gradeFont,
                        gradeVScale,
                        type,
                        triesCount),
                null);
    }

    public static SessionActivityMessage participantJoined(
            UUID sessionId, UUID userId, String userDisplayName, Instant joinedAt) {
        return new SessionActivityMessage(
                "PARTICIPANT_JOINED",
                sessionId,
                joinedAt,
                null,
                new ParticipantPayload(userId, userDisplayName, joinedAt));
    }

    public record AttemptPayload(
            UUID attemptId,
            UUID userId,
            String userDisplayName,
            UUID boulderId,
            BoulderColor boulderColor,
            String gradeFont,
            String gradeVScale,
            AttemptType type,
            int triesCount) {}

    public record ParticipantPayload(UUID userId, String userDisplayName, Instant joinedAt) {}
}
