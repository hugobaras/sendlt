package com.sendlt.support;

import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.Sector;
import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.enums.HoldStyle;
import com.sendlt.domain.enums.ScoringMode;
import java.time.LocalDate;
import java.util.UUID;

public final class TestEntityFactory {

    private TestEntityFactory() {}

    public static Sector sector(UUID gymId) {
        Sector sector = new Sector();
        sector.setId(UUID.randomUUID());
        com.sendlt.domain.entity.Gym gym = new com.sendlt.domain.entity.Gym();
        gym.setId(gymId);
        gym.setScoringMode(ScoringMode.FONT);
        sector.setGym(gym);
        sector.setName("Test sector");
        return sector;
    }

    public static Boulder boulder(BoulderColor color, String gradeFont) {
        Boulder boulder = new Boulder();
        boulder.setId(UUID.randomUUID());
        boulder.setColor(color);
        boulder.setGradeFont(gradeFont);
        boulder.setHoldStyle(HoldStyle.TECHNICAL);
        boulder.setOpenedAt(LocalDate.of(2026, 1, 1));
        boulder.setSector(sector(UUID.randomUUID()));
        return boulder;
    }
}
