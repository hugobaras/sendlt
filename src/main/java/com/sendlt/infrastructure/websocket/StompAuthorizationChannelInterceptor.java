package com.sendlt.infrastructure.websocket;

import com.sendlt.application.security.SendltPrincipal;
import com.sendlt.domain.repository.SessionParticipantRepository;
import com.sendlt.infrastructure.security.JwtService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthorizationChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final SessionParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscribe(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new AccessDeniedException("Invalid or missing JWT on STOMP CONNECT");
        }

        SendltPrincipal principal = jwtService.toPrincipal(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        accessor.setUser(authentication);
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        Optional<UUID> sessionId = StompDestinations.extractSessionId(accessor.getDestination());
        if (sessionId.isEmpty()) {
            return;
        }

        SendltPrincipal principal = getPrincipal(accessor);
        UUID userId = principal.getId();

        if (!participantRepository.existsBySessionIdAndUserId(sessionId.get(), userId)) {
            throw new AccessDeniedException("Not a member of this climbing session");
        }
    }

    private SendltPrincipal getPrincipal(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof SendltPrincipal principal) {
            return principal;
        }
        throw new AccessDeniedException("Unauthenticated STOMP session");
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authorization = accessor.getNativeHeader("Authorization");
        if (authorization != null && !authorization.isEmpty()) {
            String value = authorization.get(0);
            if (value.startsWith(BEARER_PREFIX)) {
                return value.substring(BEARER_PREFIX.length()).trim();
            }
            return value.trim();
        }

        List<String> authToken = accessor.getNativeHeader("auth-token");
        if (authToken != null && !authToken.isEmpty()) {
            return authToken.get(0).trim();
        }

        return null;
    }
}
