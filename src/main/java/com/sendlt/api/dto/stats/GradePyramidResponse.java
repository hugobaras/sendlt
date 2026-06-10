package com.sendlt.api.dto.stats;

import java.util.List;

public record GradePyramidResponse(List<GradePyramidEntry> entries) {

    public record GradePyramidEntry(String grade, long count) {}
}
