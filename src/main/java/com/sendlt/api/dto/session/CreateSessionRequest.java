package com.sendlt.api.dto.session;

import com.sendlt.domain.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateSessionRequest(@NotNull UUID gymId) {}
