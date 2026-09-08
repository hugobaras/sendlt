package com.sendlt.application.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.support.TestEntityFactory;
import org.junit.jupiter.api.Test;

class GradeScoringStrategyTest {

    private final GradeScoringStrategy strategy = new GradeScoringStrategy();

    @Test
    void scoresFontGradeUsingOrdinal() {
        Boulder fiveA = TestEntityFactory.boulder(BoulderColor.BLEU, "5a");
        Boulder sixA = TestEntityFactory.boulder(BoulderColor.ROUGE, "6a");

        assertThat(strategy.score(fiveA)).isEqualTo(FontGradeOrder.ordinal("5a"));
        assertThat(strategy.score(sixA)).isGreaterThan(strategy.score(fiveA));
    }

    @Test
    void fallsBackToVScaleWhenFontMissing() {
        Boulder v4 = TestEntityFactory.boulder(BoulderColor.VERT, null);
        v4.setGradeVScale("V4");

        assertThat(strategy.score(v4)).isEqualTo(VScaleOrder.ordinal("V4"));
        assertThat(strategy.gradeLabel(v4)).isEqualTo("V4");
    }
}
