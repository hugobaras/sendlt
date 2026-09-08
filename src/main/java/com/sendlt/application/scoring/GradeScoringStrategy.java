package com.sendlt.application.scoring;

import com.sendlt.domain.entity.Boulder;
import org.springframework.stereotype.Component;

@Component
public class GradeScoringStrategy implements ScoringStrategy {

    @Override
    public int score(Boulder boulder) {
        if (boulder.getGradeFont() != null && !boulder.getGradeFont().isBlank()) {
            return FontGradeOrder.ordinal(boulder.getGradeFont());
        }
        if (boulder.getGradeVScale() != null && !boulder.getGradeVScale().isBlank()) {
            return VScaleOrder.ordinal(boulder.getGradeVScale());
        }
        return 0;
    }

    @Override
    public String gradeLabel(Boulder boulder) {
        if (boulder.getGradeFont() != null && !boulder.getGradeFont().isBlank()) {
            return boulder.getGradeFont();
        }
        if (boulder.getGradeVScale() != null && !boulder.getGradeVScale().isBlank()) {
            return boulder.getGradeVScale();
        }
        return "UNKNOWN";
    }
}
