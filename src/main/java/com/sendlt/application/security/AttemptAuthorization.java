package com.sendlt.application.security;

import com.sendlt.application.exception.ForbiddenOperationException;
import com.sendlt.domain.repository.AttemptRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("attemptSecurity")
@RequiredArgsConstructor
public class AttemptAuthorization {

    private final AttemptRepository attemptRepository;

    public boolean isCurrentUser(UUID userId) {
        return SecurityUtils.getCurrentUserIdOptional()
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    public boolean canModifyAttempt(UUID attemptOwnerUserId) {
        return isCurrentUser(attemptOwnerUserId);
    }

    public boolean canModifyAttemptById(UUID attemptId) {
        return attemptRepository
                .findById(attemptId)
                .map(attempt -> isCurrentUser(attempt.getUser().getId()))
                .orElse(false);
    }

    public void assertCanModifyAttempt(UUID attemptOwnerUserId) {
        if (!canModifyAttempt(attemptOwnerUserId)) {
            throw new ForbiddenOperationException("You can only modify your own attempts");
        }
    }
}
