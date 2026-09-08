package com.sendlt.application.scoring;

import com.sendlt.domain.entity.Boulder;

public interface ScoringStrategy {

    int score(Boulder boulder);

    String gradeLabel(Boulder boulder);
}
