package com.sendlt.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "session_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_session_participants_session_user",
                columnNames = {"session_id", "user_id"}),
        indexes = @Index(name = "idx_session_participants_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class SessionParticipant extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ClimbingSession session;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }
}
