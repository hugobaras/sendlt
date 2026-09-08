package com.sendlt.domain.repository;

import com.sendlt.domain.entity.SessionParticipant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, UUID> {

    boolean existsBySessionIdAndUserId(UUID sessionId, UUID userId);

    Optional<SessionParticipant> findBySessionIdAndUserId(UUID sessionId, UUID userId);
}
