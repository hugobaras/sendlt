package com.sendlt.api.controller;

import com.sendlt.api.dto.session.CreateSessionRequest;
import com.sendlt.api.dto.session.JoinSessionRequest;
import com.sendlt.api.dto.session.SessionParticipantResponse;
import com.sendlt.api.dto.session.SessionResponse;
import com.sendlt.application.service.SessionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/{id}")
    public SessionResponse get(@PathVariable UUID id) {
        return sessionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse create(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.create(request);
    }

    @PostMapping("/{id}/close")
    public SessionResponse close(@PathVariable UUID id) {
        return sessionService.close(id);
    }

    @PostMapping("/join")
    public SessionParticipantResponse join(@Valid @RequestBody JoinSessionRequest request) {
        return sessionService.join(request);
    }
}
