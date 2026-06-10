package com.sendlt.api.dto.stats;

import java.time.Instant;

public record VolumeStatsResponse(long successfulBoulders, Instant from, Instant to) {}
