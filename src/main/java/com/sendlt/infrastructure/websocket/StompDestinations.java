package com.sendlt.infrastructure.websocket;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StompDestinations {

    public static final String SESSION_ACTIVITY_TEMPLATE = "/topic/sessions/%s/activity";

    private static final Pattern SESSION_ACTIVITY_PATTERN =
            Pattern.compile("^/topic/sessions/([0-9a-fA-F\\-]{36})/activity$");

    private StompDestinations() {}

    public static String sessionActivity(UUID sessionId) {
        return String.format(SESSION_ACTIVITY_TEMPLATE, sessionId);
    }

    public static Optional<UUID> extractSessionId(String destination) {
        if (destination == null) {
            return Optional.empty();
        }
        Matcher matcher = SESSION_ACTIVITY_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(matcher.group(1)));
    }
}
