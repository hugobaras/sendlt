package com.sendlt.application.event;

import java.time.Instant;
import java.util.UUID;

public record ParticipantJoinedEvent(
        UUID sessionId, UUID userId, String userDisplayName, Instant joinedAt) {}
