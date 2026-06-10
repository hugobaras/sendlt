package com.sendlt.api.dto.stats;

public record SessionAverageGradeResponse(
        String averageGradeLabel, double averageScore, long successfulAttempts) {}
