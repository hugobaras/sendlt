package com.sendlt.support;

import com.sendlt.application.security.SendltPrincipal;
import com.sendlt.domain.enums.UserRole;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TestSecuritySupport {

    private TestSecuritySupport() {}

    public static void authenticate(UUID userId, UserRole role) {
        SendltPrincipal principal = new SendltPrincipal(userId, userId + "@test.sendlt", role);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
