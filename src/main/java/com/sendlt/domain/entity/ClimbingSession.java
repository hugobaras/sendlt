package com.sendlt.domain.entity;

import com.sendlt.domain.enums.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "climbing_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_climbing_sessions_room_code", columnNames = "room_code"),
        indexes = @Index(name = "idx_climbing_sessions_gym_id", columnList = "gym_id"))
@Getter
@Setter
@NoArgsConstructor
public class ClimbingSession extends BaseEntity {

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{6}$", message = "roomCode must be exactly 6 uppercase alphanumeric characters")
    @Column(name = "room_code", nullable = false, length = 6)
    private String roomCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SessionStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    private List<SessionParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    private List<Attempt> attempts = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = SessionStatus.ACTIVE;
        }
    }
}
