package com.sendlt.application.service;

import com.sendlt.api.dto.attempt.AttemptResponse;
import com.sendlt.api.dto.attempt.CreateAttemptRequest;
import com.sendlt.api.dto.attempt.UpdateAttemptRequest;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.event.AttemptCreatedEvent;
import com.sendlt.application.exception.BusinessValidationException;
import com.sendlt.application.exception.ForbiddenOperationException;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.application.security.SecurityUtils;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.repository.AttemptRepository;
import com.sendlt.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final SessionService sessionService;
    private final BoulderService boulderService;
    private final UserRepository userRepository;
    private final DomainMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AttemptResponse create(UUID sessionId, CreateAttemptRequest request) {
        ClimbingSession session = sessionService.getSession(sessionId);
        sessionService.assertSessionActive(session);
        sessionService.assertCurrentUserIsParticipant(sessionId);

        validateAttemptRules(request.type(), request.triesCount());

        UUID currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository
                .findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));
        Boulder boulder = boulderService.getBoulder(request.boulderId());

        Attempt attempt = new Attempt();
        attempt.setSession(session);
        attempt.setUser(user);
        attempt.setBoulder(boulder);
        attempt.setType(request.type());
        attempt.setTriesCount(request.triesCount());

        Attempt saved = attemptRepository.save(attempt);
        eventPublisher.publishEvent(toCreatedEvent(saved, user, boulder));
        return mapper.toAttemptResponse(saved);
    }

    @Transactional
    public AttemptResponse update(UUID sessionId, UUID attemptId, UpdateAttemptRequest request) {
        Attempt attempt = getAttemptForSession(sessionId, attemptId);
        assertOwnership(attempt);

        validateAttemptRules(request.type(), request.triesCount());
        attempt.setType(request.type());
        attempt.setTriesCount(request.triesCount());

        return mapper.toAttemptResponse(attemptRepository.save(attempt));
    }

    @Transactional
    public void delete(UUID sessionId, UUID attemptId) {
        Attempt attempt = getAttemptForSession(sessionId, attemptId);
        assertOwnership(attempt);
        attemptRepository.delete(attempt);
    }

    private AttemptCreatedEvent toCreatedEvent(Attempt attempt, User user, Boulder boulder) {
        return new AttemptCreatedEvent(
                attempt.getId(),
                attempt.getSession().getId(),
                boulder.getId(),
                user.getId(),
                user.getDisplayName(),
                boulder.getColor(),
                boulder.getGradeFont(),
                boulder.getGradeVScale(),
                attempt.getType(),
                attempt.getTriesCount(),
                attempt.getCreatedAt());
    }

    void validateAttemptRules(AttemptType type, int triesCount) {
        if (type == AttemptType.FLASH && triesCount != 1) {
            throw new BusinessValidationException("A FLASH attempt must have triesCount = 1");
        }
    }

    private Attempt getAttemptForSession(UUID sessionId, UUID attemptId) {
        Attempt attempt = attemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        if (!attempt.getSession().getId().equals(sessionId)) {
            throw new ResourceNotFoundException("Attempt", attemptId);
        }
        return attempt;
    }

    private void assertOwnership(Attempt attempt) {
        if (!attempt.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenOperationException("You can only modify your own attempts");
        }
    }
}
