package com.sendlt.domain.entity;

import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.enums.HoldStyle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "boulders",
        indexes = @Index(name = "idx_boulders_sector_id", columnList = "sector_id"))
@Getter
@Setter
@NoArgsConstructor
public class Boulder extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 32)
    private BoulderColor color;

    @Column(name = "grade_font", length = 16)
    private String gradeFont;

    @Column(name = "grade_v_scale", length = 16)
    private String gradeVScale;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hold_style", nullable = false, length = 32)
    private HoldStyle holdStyle;

    @NotNull
    @Column(name = "opened_at", nullable = false)
    private LocalDate openedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;
}
