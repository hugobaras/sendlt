package com.sendlt.application.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.sendlt.domain.repository.AttemptRepository;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.User;
import com.sendlt.domain.enums.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttemptAuthorizationTest {

    @Mock
    private AttemptRepository attemptRepository;

    @InjectMocks
    private AttemptAuthorization attemptAuthorization;

    private final UUID currentUserId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        com.sendlt.support.TestSecuritySupport.authenticate(currentUserId, UserRole.CLIMBER);
    }

    @AfterEach
    void tearDown() {
        com.sendlt.support.TestSecuritySupport.clear();
    }

    @Test
    void canModifyAttemptReturnsTrueForOwner() {
        assertThat(attemptAuthorization.canModifyAttempt(currentUserId)).isTrue();
    }

    @Test
    void canModifyAttemptReturnsFalseForOtherUser() {
        assertThat(attemptAuthorization.canModifyAttempt(otherUserId)).isFalse();
    }

    @Test
    void canModifyAttemptByIdUsesRepositoryOwner() {
        UUID attemptId = UUID.randomUUID();
        Attempt attempt = new Attempt();
        User owner = new User();
        owner.setId(currentUserId);
        attempt.setUser(owner);
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        assertThat(attemptAuthorization.canModifyAttemptById(attemptId)).isTrue();
    }
}
