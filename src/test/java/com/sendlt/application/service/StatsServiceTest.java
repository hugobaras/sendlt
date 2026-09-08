package com.sendlt.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sendlt.api.dto.stats.GradePyramidResponse;
import com.sendlt.api.dto.stats.SessionAverageGradeResponse;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.ScoringMode;
import com.sendlt.domain.enums.UserRole;
import com.sendlt.domain.repository.AttemptRepository;
import com.sendlt.application.scoring.FontGradeOrder;
import com.sendlt.application.scoring.GradeScoringStrategy;
import com.sendlt.application.scoring.ScoringStrategyFactory;
import com.sendlt.support.TestEntityFactory;
import com.sendlt.support.TestSecuritySupport;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private GymService gymService;

    @Mock
    private ScoringStrategyFactory scoringStrategyFactory;

    @InjectMocks
    private StatsService statsService;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TestSecuritySupport.authenticate(userId, UserRole.CLIMBER);
    }

    @AfterEach
    void tearDown() {
        TestSecuritySupport.clear();
    }

    @Test
    void gradePyramidAggregatesAndSortsByFontOrdinal() {
        when(attemptRepository.gradePyramidByFont(eq(userId), any(), any(), any()))
                .thenReturn(List.of(new Object[] {"6a", 2L}, new Object[] {"5a", 1L}));

        GradePyramidResponse pyramid = statsService.gradePyramid(null, null);

        assertThat(pyramid.entries()).hasSize(2);
        assertThat(pyramid.entries().get(0).grade()).isEqualTo("5a");
        assertThat(pyramid.entries().get(0).count()).isEqualTo(1);
        assertThat(pyramid.entries().get(1).grade()).isEqualTo("6a");
        assertThat(pyramid.entries().get(1).count()).isEqualTo(2);
    }

    @Test
    void sessionAverageGradeComputesMeanFontScore() {
        Gym gym = new Gym();
        gym.setId(UUID.randomUUID());
        gym.setScoringMode(ScoringMode.FONT);

        ClimbingSession session = new ClimbingSession();
        session.setId(sessionId);
        session.setGym(gym);

        Boulder fiveA = TestEntityFactory.boulder(com.sendlt.domain.enums.BoulderColor.BLEU, "5a");
        Boulder sixA = TestEntityFactory.boulder(com.sendlt.domain.enums.BoulderColor.ROUGE, "6a");

        Attempt attempt1 = successfulAttempt(fiveA);
        Attempt attempt2 = successfulAttempt(sixA);

        when(sessionService.getSession(sessionId)).thenReturn(session);
        when(gymService.getGym(gym.getId())).thenReturn(gym);
        when(scoringStrategyFactory.forGym(gym)).thenReturn(new GradeScoringStrategy());
        when(attemptRepository.findSuccessfulAttemptsInSession(eq(sessionId), eq(userId), any()))
                .thenReturn(List.of(attempt1, attempt2));

        SessionAverageGradeResponse average = statsService.sessionAverageGrade(sessionId);

        double expectedAverage =
                (FontGradeOrder.ordinal("5a") + FontGradeOrder.ordinal("6a")) / 2.0;
        assertThat(average.successfulAttempts()).isEqualTo(2);
        assertThat(average.averageScore()).isEqualTo(expectedAverage);
        assertThat(average.averageGradeLabel()).isNotBlank();
    }

    private Attempt successfulAttempt(Boulder boulder) {
        Attempt attempt = new Attempt();
        attempt.setId(UUID.randomUUID());
        attempt.setBoulder(boulder);
        attempt.setType(AttemptType.FLASH);
        attempt.setTriesCount(1);
        attempt.setCreatedAt(Instant.now());
        return attempt;
    }
}
