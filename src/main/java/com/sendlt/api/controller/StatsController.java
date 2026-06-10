package com.sendlt.api.controller;

import com.sendlt.api.dto.stats.GradePyramidResponse;
import com.sendlt.api.dto.stats.SessionAverageGradeResponse;
import com.sendlt.api.dto.stats.VolumeStatsResponse;
import com.sendlt.application.service.StatsService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/me/volume")
    public VolumeStatsResponse volume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return statsService.volume(from, to);
    }

    @GetMapping("/me/grade-pyramid")
    public GradePyramidResponse gradePyramid(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return statsService.gradePyramid(from, to);
    }

    @GetMapping("/sessions/{sessionId}/average-grade")
    public SessionAverageGradeResponse sessionAverageGrade(@PathVariable UUID sessionId) {
        return statsService.sessionAverageGrade(sessionId);
    }
}
