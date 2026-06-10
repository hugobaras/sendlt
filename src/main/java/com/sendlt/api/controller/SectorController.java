package com.sendlt.api.controller;

import com.sendlt.api.dto.sector.CreateSectorRequest;
import com.sendlt.api.dto.sector.SectorResponse;
import com.sendlt.api.dto.sector.UpdateSectorRequest;
import com.sendlt.application.service.SectorService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService sectorService;

    @GetMapping
    public List<SectorResponse> list(@RequestParam UUID gymId) {
        return sectorService.findByGym(gymId);
    }

    @GetMapping("/{id}")
    public SectorResponse get(@PathVariable UUID id) {
        return sectorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectorResponse create(@Valid @RequestBody CreateSectorRequest request) {
        return sectorService.create(request);
    }

    @PutMapping("/{id}")
    public SectorResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSectorRequest request) {
        return sectorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectorService.delete(id);
    }
}
