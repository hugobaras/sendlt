package com.sendlt.application.scoring;

import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.enums.ScoringMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoringStrategyFactory {

    private final ColorScoringStrategy colorScoringStrategy;
    private final GradeScoringStrategy gradeScoringStrategy;

    public ScoringStrategy forGym(Gym gym) {
        if (gym.getScoringMode() == ScoringMode.COLOR) {
            return colorScoringStrategy;
        }
        return gradeScoringStrategy;
    }
}
