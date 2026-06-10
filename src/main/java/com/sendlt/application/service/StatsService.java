package com.sendlt.application.service;

import com.sendlt.api.dto.stats.GradePyramidResponse;
import com.sendlt.api.dto.stats.SessionAverageGradeResponse;
import com.sendlt.api.dto.stats.VolumeStatsResponse;
import com.sendlt.application.scoring.FontGradeOrder;
import com.sendlt.application.scoring.ScoringStrategy;
import com.sendlt.application.scoring.ScoringStrategyFactory;
import com.sendlt.application.security.SecurityUtils;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.enums.AttemptType;
import com.sendlt.domain.enums.ScoringMode;
import com.sendlt.domain.repository.AttemptRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final List<AttemptType> SUCCESS_TYPES = List.of(AttemptType.FLASH, AttemptType.TOP_AFTER_TRIES);

    private final AttemptRepository attemptRepository;
    private final SessionService sessionService;
    private final GymService gymService;
    private final ScoringStrategyFactory scoringStrategyFactory;

    @Transactional(readOnly = true)
    public VolumeStatsResponse volume(Instant from, Instant to) {
        Instant rangeFrom = from != null ? from : Instant.EPOCH;
        Instant rangeTo = to != null ? to : Instant.now();
        UUID userId = SecurityUtils.getCurrentUserId();
        long count = attemptRepository.countDistinctSuccessfulBoulders(userId, SUCCESS_TYPES, rangeFrom, rangeTo);
        return new VolumeStatsResponse(count, rangeFrom, rangeTo);
    }

    @Transactional(readOnly = true)
    public GradePyramidResponse gradePyramid(Instant from, Instant to) {
        Instant rangeFrom = from != null ? from : Instant.EPOCH;
        Instant rangeTo = to != null ? to : Instant.now();
        UUID userId = SecurityUtils.getCurrentUserId();

        List<Object[]> rows = attemptRepository.gradePyramidByFont(userId, SUCCESS_TYPES, rangeFrom, rangeTo);
        List<GradePyramidResponse.GradePyramidEntry> entries = rows.stream()
                .map(row -> new GradePyramidResponse.GradePyramidEntry((String) row[0], (Long) row[1]))
                .sorted((a, b) -> Integer.compare(FontGradeOrder.ordinal(a.grade()), FontGradeOrder.ordinal(b.grade())))
                .toList();

        return new GradePyramidResponse(entries);
    }

    @Transactional(readOnly = true)
    public SessionAverageGradeResponse sessionAverageGrade(UUID sessionId) {
        ClimbingSession session = sessionService.getSession(sessionId);
        Gym gym = gymService.getGym(session.getGym().getId());
        ScoringStrategy strategy = scoringStrategyFactory.forGym(gym);

        UUID userId = SecurityUtils.getCurrentUserId();
        List<Attempt> attempts =
                attemptRepository.findSuccessfulAttemptsInSession(sessionId, userId, SUCCESS_TYPES);

        if (attempts.isEmpty()) {
            return new SessionAverageGradeResponse("N/A", 0.0, 0);
        }

        double averageScore =
                attempts.stream().mapToInt(a -> strategy.score(a.getBoulder())).average().orElse(0.0);

        String averageLabel = formatAverageLabel(gym.getScoringMode(), averageScore);
        return new SessionAverageGradeResponse(averageLabel, averageScore, attempts.size());
    }

    private String formatAverageLabel(ScoringMode mode, double averageScore) {
        if (mode == ScoringMode.COLOR) {
            int rounded = (int) Math.round(averageScore);
            return switch (rounded) {
                case 10 -> "JAUNE";
                case 20 -> "VERT";
                case 30 -> "BLEU";
                case 40 -> "VIOLET";
                case 50 -> "ROUGE";
                case 60 -> "BLANC";
                case 70 -> "NOIR";
                default -> String.valueOf(rounded);
            };
        }
        int ordinal = Math.max(1, (int) Math.round(averageScore));
        return FontGradeOrder.fromOrdinal(ordinal);
    }
}
