package com.sendlt.api.controller;

import com.sendlt.api.dto.attempt.AttemptResponse;
import com.sendlt.api.dto.attempt.CreateAttemptRequest;
import com.sendlt.api.dto.attempt.UpdateAttemptRequest;
import com.sendlt.application.service.AttemptService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions/{sessionId}/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptResponse create(
            @PathVariable UUID sessionId, @Valid @RequestBody CreateAttemptRequest request) {
        return attemptService.create(sessionId, request);
    }

    @PutMapping("/{attemptId}")
    public AttemptResponse update(
            @PathVariable UUID sessionId,
            @PathVariable UUID attemptId,
            @Valid @RequestBody UpdateAttemptRequest request) {
        return attemptService.update(sessionId, attemptId, request);
    }

    @DeleteMapping("/{attemptId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID sessionId, @PathVariable UUID attemptId) {
        attemptService.delete(sessionId, attemptId);
    }
}
