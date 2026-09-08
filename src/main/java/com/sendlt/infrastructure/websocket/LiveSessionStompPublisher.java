package com.sendlt.infrastructure.websocket;

import com.sendlt.api.dto.live.SessionActivityMessage;
import com.sendlt.application.event.AttemptCreatedEvent;
import com.sendlt.application.event.ParticipantJoinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LiveSessionStompPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttemptCreated(AttemptCreatedEvent event) {
        SessionActivityMessage message = SessionActivityMessage.attemptCreated(
                event.sessionId(),
                event.attemptId(),
                event.userId(),
                event.userDisplayName(),
                event.boulderId(),
                event.boulderColor(),
                event.gradeFont(),
                event.gradeVScale(),
                event.type(),
                event.triesCount(),
                event.createdAt());
        messagingTemplate.convertAndSend(StompDestinations.sessionActivity(event.sessionId()), message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onParticipantJoined(ParticipantJoinedEvent event) {
        SessionActivityMessage message = SessionActivityMessage.participantJoined(
                event.sessionId(), event.userId(), event.userDisplayName(), event.joinedAt());
        messagingTemplate.convertAndSend(StompDestinations.sessionActivity(event.sessionId()), message);
    }
}
