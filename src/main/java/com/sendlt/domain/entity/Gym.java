package com.sendlt.domain.entity;

import com.sendlt.domain.enums.ScoringMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gyms")
@Getter
@Setter
@NoArgsConstructor
public class Gym extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city")
    private String city;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scoring_mode", nullable = false, length = 32)
    private ScoringMode scoringMode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
    private List<Sector> sectors = new ArrayList<>();

    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
    private List<ClimbingSession> sessions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
