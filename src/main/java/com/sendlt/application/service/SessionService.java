package com.sendlt.application.service;

import com.sendlt.api.dto.session.CreateSessionRequest;
import com.sendlt.api.dto.session.JoinSessionRequest;
import com.sendlt.api.dto.session.SessionParticipantResponse;
import com.sendlt.api.dto.session.SessionResponse;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.event.ParticipantJoinedEvent;
import com.sendlt.application.exception.BusinessValidationException;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.application.security.SecurityUtils;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.entity.SessionParticipant;
import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.SessionStatus;
import com.sendlt.domain.repository.ClimbingSessionRepository;
import com.sendlt.domain.repository.SessionParticipantRepository;
import com.sendlt.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String ROOM_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private static final int MAX_ROOM_CODE_ATTEMPTS = 20;

    private final ClimbingSessionRepository sessionRepository;
    private final SessionParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GymService gymService;
    private final DomainMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(readOnly = true)
    public SessionResponse findById(UUID id) {
        return mapper.toSessionResponse(getSession(id));
    }

    @Transactional
    public SessionResponse create(CreateSessionRequest request) {
        Gym gym = gymService.getGym(request.gymId());
        ClimbingSession session = new ClimbingSession();
        session.setGym(gym);
        session.setRoomCode(generateUniqueRoomCode());
        session.setStatus(SessionStatus.ACTIVE);
        ClimbingSession saved = sessionRepository.save(session);
        joinAsCurrentUser(saved);
        return mapper.toSessionResponse(saved);
    }

    @Transactional
    public SessionResponse close(UUID sessionId) {
        ClimbingSession session = getSession(sessionId);
        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BusinessValidationException("Session is already closed");
        }
        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        return mapper.toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionParticipantResponse join(JoinSessionRequest request) {
        ClimbingSession session = sessionRepository
                .findByRoomCode(request.roomCode())
                .orElseThrow(() -> new ResourceNotFoundException("ClimbingSession", request.roomCode()));
        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BusinessValidationException("Cannot join a closed session");
        }
        return mapper.toParticipantResponse(joinAsCurrentUser(session));
    }

    ClimbingSession getSession(UUID id) {
        return sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ClimbingSession", id));
    }

    void assertSessionActive(ClimbingSession session) {
        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BusinessValidationException("Session is closed");
        }
    }

    void assertCurrentUserIsParticipant(UUID sessionId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!participantRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new BusinessValidationException("You must join the session before recording attempts");
        }
    }

    private SessionParticipant joinAsCurrentUser(ClimbingSession session) {
        UUID userId = SecurityUtils.getCurrentUserId();
        return participantRepository
                .findBySessionIdAndUserId(session.getId(), userId)
                .orElseGet(() -> {
                    User user = userRepository
                            .findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    SessionParticipant participant = new SessionParticipant();
                    participant.setSession(session);
                    participant.setUser(user);
                    SessionParticipant saved = participantRepository.save(participant);
                    eventPublisher.publishEvent(new ParticipantJoinedEvent(
                            session.getId(), user.getId(), user.getDisplayName(), saved.getJoinedAt()));
                    return saved;
                });
    }

    private String generateUniqueRoomCode() {
        for (int attempt = 0; attempt < MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            String code = randomRoomCode();
            if (!sessionRepository.existsByRoomCode(code)) {
                return code;
            }
        }
        throw new BusinessValidationException("Unable to generate a unique room code");
    }

    private String randomRoomCode() {
        StringBuilder builder = new StringBuilder(ROOM_CODE_LENGTH);
        for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
            builder.append(ROOM_CODE_ALPHABET.charAt(secureRandom.nextInt(ROOM_CODE_ALPHABET.length())));
        }
        return builder.toString();
    }
}
