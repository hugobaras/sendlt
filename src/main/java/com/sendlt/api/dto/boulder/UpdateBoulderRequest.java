package com.sendlt.api.dto.boulder;

import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.enums.HoldStyle;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateBoulderRequest(
        @NotNull BoulderColor color,
        String gradeFont,
        String gradeVScale,
        @NotNull HoldStyle holdStyle,
        @NotNull LocalDate openedAt) {}
