package com.sendlt.api.dto.session;

import java.time.Instant;
import java.util.UUID;

public record SessionParticipantResponse(UUID id, UUID sessionId, UUID userId, Instant joinedAt) {}
