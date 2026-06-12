package com.sendlt.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sendlt.api.dto.attempt.CreateAttemptRequest;
import com.sendlt.api.dto.attempt.UpdateAttemptRequest;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.event.AttemptCreatedEvent;
import com.sendlt.application.exception.BusinessValidationException;
import com.sendlt.application.exception.ForbiddenOperationException;
import com.sendlt.application.security.SendltPrincipal;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.SessionStatus;
import com.sendlt.domain.enums.UserRole;
import com.sendlt.domain.repository.AttemptRepository;
import com.sendlt.domain.repository.UserRepository;
import com.sendlt.support.TestEntityFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private BoulderService boulderService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DomainMapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AttemptService attemptService;

    private final UUID userId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID boulderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SendltPrincipal principal = new SendltPrincipal(userId, "climber@test.sendlt", UserRole.CLIMBER);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void flashRuleRejectsTriesCountGreaterThanOne() {
        assertThatThrownBy(() -> attemptService.validateAttemptRules(AttemptType.FLASH, 3))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("A FLASH attempt must have triesCount = 1");
    }

    @Test
    void flashRuleAcceptsTriesCountEqualToOne() {
        attemptService.validateAttemptRules(AttemptType.FLASH, 1);
    }

    @Test
    void createPublishesAttemptCreatedEvent() {
        ClimbingSession session = activeSession();
        User user = user(userId);
        Boulder boulder = TestEntityFactory.boulder(com.sendlt.domain.enums.BoulderColor.BLEU, "5a");
        boulder.setId(boulderId);

        when(sessionService.getSession(sessionId)).thenReturn(session);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(boulderService.getBoulder(boulderId)).thenReturn(boulder);
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(invocation -> {
            Attempt attempt = invocation.getArgument(0);
            attempt.setId(UUID.randomUUID());
            return attempt;
        });

        attemptService.create(sessionId, new CreateAttemptRequest(boulderId, AttemptType.FLASH, 1));

        ArgumentCaptor<AttemptCreatedEvent> captor = ArgumentCaptor.forClass(AttemptCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(AttemptType.FLASH);
        assertThat(captor.getValue().triesCount()).isEqualTo(1);
    }

    @Test
    void updateRejectsAttemptOwnedByAnotherUser() {
        Attempt attempt = attemptOwnedBy(otherUserId);

        when(attemptRepository.findById(attempt.getId())).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> attemptService.update(
                        sessionId, attempt.getId(), new UpdateAttemptRequest(AttemptType.TOP_AFTER_TRIES, 2)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You can only modify your own attempts");
    }

    @Test
    void deleteRejectsAttemptOwnedByAnotherUser() {
        Attempt attempt = attemptOwnedBy(otherUserId);

        when(attemptRepository.findById(attempt.getId())).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> attemptService.delete(sessionId, attempt.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private ClimbingSession activeSession() {
        ClimbingSession session = new ClimbingSession();
        session.setId(sessionId);
        session.setStatus(SessionStatus.ACTIVE);
        return session;
    }

    private User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setDisplayName("Climber");
        user.setEmail("climber@test.sendlt");
        user.setPasswordHash("hash");
        user.setRole(UserRole.CLIMBER);
        return user;
    }

    private Attempt attemptOwnedBy(UUID ownerId) {
        Attempt attempt = new Attempt();
        attempt.setId(UUID.randomUUID());
        attempt.setSession(activeSession());
        attempt.setUser(user(ownerId));
        attempt.setBoulder(TestEntityFactory.boulder(com.sendlt.domain.enums.BoulderColor.BLEU, "5a"));
        attempt.setType(AttemptType.FLASH);
        attempt.setTriesCount(1);
        return attempt;
    }
}
