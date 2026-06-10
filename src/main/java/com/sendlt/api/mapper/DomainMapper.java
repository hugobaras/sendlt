package com.sendlt.api.mapper;

import com.sendlt.api.dto.attempt.AttemptResponse;
import com.sendlt.api.dto.boulder.BoulderResponse;
import com.sendlt.api.dto.gym.GymResponse;
import com.sendlt.api.dto.sector.SectorResponse;
import com.sendlt.api.dto.session.SessionParticipantResponse;
import com.sendlt.api.dto.session.SessionResponse;
import com.sendlt.domain.entity.Attempt;
import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.ClimbingSession;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.entity.Sector;
import com.sendlt.domain.entity.SessionParticipant;
import org.springframework.stereotype.Component;

@Component
public class DomainMapper {

    public GymResponse toGymResponse(Gym gym) {
        return new GymResponse(gym.getId(), gym.getName(), gym.getCity(), gym.getScoringMode(), gym.getCreatedAt());
    }

    public SectorResponse toSectorResponse(Sector sector) {
        return new SectorResponse(
                sector.getId(), sector.getGym().getId(), sector.getName(), sector.getCreatedAt());
    }

    public BoulderResponse toBoulderResponse(Boulder boulder) {
        return new BoulderResponse(
                boulder.getId(),
                boulder.getSector().getId(),
                boulder.getColor(),
                boulder.getGradeFont(),
                boulder.getGradeVScale(),
                boulder.getHoldStyle(),
                boulder.getOpenedAt());
    }

    public SessionResponse toSessionResponse(ClimbingSession session) {
        return new SessionResponse(
                session.getId(),
                session.getRoomCode(),
                session.getStatus(),
                session.getGym().getId(),
                session.getCreatedAt(),
                session.getClosedAt());
    }

    public SessionParticipantResponse toParticipantResponse(SessionParticipant participant) {
        return new SessionParticipantResponse(
                participant.getId(),
                participant.getSession().getId(),
                participant.getUser().getId(),
                participant.getJoinedAt());
    }

    public AttemptResponse toAttemptResponse(Attempt attempt) {
        return new AttemptResponse(
                attempt.getId(),
                attempt.getBoulder().getId(),
                attempt.getUser().getId(),
                attempt.getSession().getId(),
                attempt.getType(),
                attempt.getTriesCount(),
                attempt.getCreatedAt());
    }
}
