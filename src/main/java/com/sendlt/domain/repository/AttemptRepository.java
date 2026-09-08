package com.sendlt.domain.repository;

import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.enums.AttemptType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    List<Attempt> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    @Query(
            """
            SELECT COUNT(DISTINCT a.boulder.id)
            FROM Attempt a
            WHERE a.user.id = :userId
              AND a.type IN :successTypes
              AND a.createdAt >= :from
              AND a.createdAt < :to
            """)
    long countDistinctSuccessfulBoulders(
            @Param("userId") UUID userId,
            @Param("successTypes") List<AttemptType> successTypes,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query(
            """
            SELECT b.gradeFont, COUNT(DISTINCT a.boulder.id)
            FROM Attempt a
            JOIN a.boulder b
            WHERE a.user.id = :userId
              AND a.type IN :successTypes
              AND a.createdAt >= :from
              AND a.createdAt < :to
              AND b.gradeFont IS NOT NULL
            GROUP BY b.gradeFont
            ORDER BY b.gradeFont
            """)
    List<Object[]> gradePyramidByFont(
            @Param("userId") UUID userId,
            @Param("successTypes") List<AttemptType> successTypes,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query(
            """
            SELECT b.color, COUNT(DISTINCT a.boulder.id)
            FROM Attempt a
            JOIN a.boulder b
            WHERE a.user.id = :userId
              AND a.type IN :successTypes
              AND a.createdAt >= :from
              AND a.createdAt < :to
            GROUP BY b.color
            ORDER BY b.color
            """)
    List<Object[]> gradePyramidByColor(
            @Param("userId") UUID userId,
            @Param("successTypes") List<AttemptType> successTypes,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query(
            """
            SELECT a
            FROM Attempt a
            JOIN FETCH a.boulder b
            WHERE a.session.id = :sessionId
              AND a.user.id = :userId
              AND a.type IN :successTypes
            """)
    List<Attempt> findSuccessfulAttemptsInSession(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId,
            @Param("successTypes") List<AttemptType> successTypes);
}
