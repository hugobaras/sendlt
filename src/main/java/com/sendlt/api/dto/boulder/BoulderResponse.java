package com.sendlt.api.dto.boulder;

import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.enums.HoldStyle;
import java.time.LocalDate;
import java.util.UUID;

public record BoulderResponse(
        UUID id,
        UUID sectorId,
        BoulderColor color,
        String gradeFont,
        String gradeVScale,
        HoldStyle holdStyle,
        LocalDate openedAt) {}
