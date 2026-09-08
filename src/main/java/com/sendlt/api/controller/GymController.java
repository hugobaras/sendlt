package com.sendlt.api.controller;

import com.sendlt.api.dto.gym.CreateGymRequest;
import com.sendlt.api.dto.gym.GymResponse;
import com.sendlt.api.dto.gym.UpdateGymRequest;
import com.sendlt.application.service.GymService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @GetMapping
    public List<GymResponse> list() {
        return gymService.findAll();
    }

    @GetMapping("/{id}")
    public GymResponse get(@PathVariable UUID id) {
        return gymService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GymResponse create(@Valid @RequestBody CreateGymRequest request) {
        return gymService.create(request);
    }

    @PutMapping("/{id}")
    public GymResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateGymRequest request) {
        return gymService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        gymService.delete(id);
    }
}
