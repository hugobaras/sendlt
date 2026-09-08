package com.sendlt.api.controller;

import com.sendlt.api.dto.boulder.BoulderResponse;
import com.sendlt.api.dto.boulder.CreateBoulderRequest;
import com.sendlt.api.dto.boulder.UpdateBoulderRequest;
import com.sendlt.application.service.BoulderService;
import com.sendlt.domain.enums.BoulderColor;
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
@RequestMapping("/api/boulders")
@RequiredArgsConstructor
public class BoulderController {

    private final BoulderService boulderService;

    @GetMapping
    public List<BoulderResponse> search(
            @RequestParam(required = false) UUID sectorId,
            @RequestParam(required = false) BoulderColor color,
            @RequestParam(required = false) String gradeFontMin,
            @RequestParam(required = false) String gradeFontMax,
            @RequestParam(required = false) String gradeVScaleMin,
            @RequestParam(required = false) String gradeVScaleMax) {
        return boulderService.search(
                sectorId, color, gradeFontMin, gradeFontMax, gradeVScaleMin, gradeVScaleMax);
    }

    @GetMapping("/{id}")
    public BoulderResponse get(@PathVariable UUID id) {
        return boulderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoulderResponse create(@Valid @RequestBody CreateBoulderRequest request) {
        return boulderService.create(request);
    }

    @PutMapping("/{id}")
    public BoulderResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateBoulderRequest request) {
        return boulderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        boulderService.delete(id);
    }
}
