package com.sendlt.application.scoring;

import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.enums.BoulderColor;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ColorScoringStrategy implements ScoringStrategy {

    private static final Map<BoulderColor, Integer> COLOR_SCORES = Map.of(
            BoulderColor.JAUNE, 10,
            BoulderColor.VERT, 20,
            BoulderColor.BLEU, 30,
            BoulderColor.VIOLET, 40,
            BoulderColor.ROUGE, 50,
            BoulderColor.BLANC, 60,
            BoulderColor.NOIR, 70);

    @Override
    public int score(Boulder boulder) {
        return COLOR_SCORES.getOrDefault(boulder.getColor(), 0);
    }

    @Override
    public String gradeLabel(Boulder boulder) {
        return boulder.getColor().name();
    }
}
