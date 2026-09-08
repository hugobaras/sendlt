package com.sendlt.api.dto.auth;

import com.sendlt.domain.enums.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, UserRole role) {}
