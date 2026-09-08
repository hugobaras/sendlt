package com.sendlt.application.security;

import com.sendlt.domain.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<SendltPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SendltPrincipal sendltPrincipal) {
            return Optional.of(sendltPrincipal);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentPrincipal()
                .map(SendltPrincipal::getId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static Optional<UUID> getCurrentUserIdOptional() {
        return getCurrentPrincipal().map(SendltPrincipal::getId);
    }

    public static boolean hasRole(UserRole role) {
        return getCurrentPrincipal()
                .map(principal -> principal.getRole() == role)
                .orElse(false);
    }
}
