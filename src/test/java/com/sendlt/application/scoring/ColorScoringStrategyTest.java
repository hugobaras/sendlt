package com.sendlt.application.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.support.TestEntityFactory;
import org.junit.jupiter.api.Test;

class ColorScoringStrategyTest {

    private final ColorScoringStrategy strategy = new ColorScoringStrategy();

    @Test
    void scoresKnownColorsInAscendingDifficulty() {
        Boulder jaune = TestEntityFactory.boulder(BoulderColor.JAUNE, null);
        Boulder noir = TestEntityFactory.boulder(BoulderColor.NOIR, null);

        assertThat(strategy.score(jaune)).isEqualTo(10);
        assertThat(strategy.score(noir)).isEqualTo(70);
        assertThat(strategy.score(jaune)).isLessThan(strategy.score(noir));
    }

    @Test
    void gradeLabelReturnsColorName() {
        Boulder bleu = TestEntityFactory.boulder(BoulderColor.BLEU, "5a");
        assertThat(strategy.gradeLabel(bleu)).isEqualTo("BLEU");
    }
}
