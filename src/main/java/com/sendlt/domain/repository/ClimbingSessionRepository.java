package com.sendlt.domain.repository;

import com.sendlt.domain.entity.ClimbingSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClimbingSessionRepository extends JpaRepository<ClimbingSession, UUID> {

    Optional<ClimbingSession> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);
}
