package com.sendlt.application.service;

import com.sendlt.api.dto.boulder.BoulderResponse;
import com.sendlt.api.dto.boulder.CreateBoulderRequest;
import com.sendlt.api.dto.boulder.UpdateBoulderRequest;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.application.scoring.FontGradeOrder;
import com.sendlt.application.scoring.VScaleOrder;
import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.entity.Sector;
import com.sendlt.domain.enums.BoulderColor;
import com.sendlt.domain.repository.BoulderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoulderService {

    private final BoulderRepository boulderRepository;
    private final SectorService sectorService;
    private final DomainMapper mapper;

    @Transactional(readOnly = true)
    public List<BoulderResponse> search(
            UUID sectorId,
            BoulderColor color,
            String gradeFontMin,
            String gradeFontMax,
            String gradeVScaleMin,
            String gradeVScaleMax) {

        List<Boulder> boulders;
        if (sectorId != null && color != null) {
            boulders = boulderRepository.findBySectorIdAndColorOrderByOpenedAtDesc(sectorId, color);
        } else if (sectorId != null) {
            boulders = boulderRepository.findBySectorIdOrderByOpenedAtDesc(sectorId);
        } else {
            boulders = boulderRepository.findAll();
        }

        return boulders.stream()
                .filter(b -> matchesGradeFilters(b, gradeFontMin, gradeFontMax, gradeVScaleMin, gradeVScaleMax))
                .map(mapper::toBoulderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoulderResponse findById(UUID id) {
        return mapper.toBoulderResponse(getBoulder(id));
    }

    @Transactional
    public BoulderResponse create(CreateBoulderRequest request) {
        Sector sector = sectorService.getSector(request.sectorId());
        Boulder boulder = new Boulder();
        boulder.setSector(sector);
        applyFields(boulder, request.color(), request.gradeFont(), request.gradeVScale(), request.holdStyle(), request.openedAt());
        return mapper.toBoulderResponse(boulderRepository.save(boulder));
    }

    @Transactional
    public BoulderResponse update(UUID id, UpdateBoulderRequest request) {
        Boulder boulder = getBoulder(id);
        applyFields(
                boulder,
                request.color(),
                request.gradeFont(),
                request.gradeVScale(),
                request.holdStyle(),
                request.openedAt());
        return mapper.toBoulderResponse(boulderRepository.save(boulder));
    }

    @Transactional
    public void delete(UUID id) {
        if (!boulderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Boulder", id);
        }
        boulderRepository.deleteById(id);
    }

    Boulder getBoulder(UUID id) {
        return boulderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Boulder", id));
    }

    private void applyFields(
            Boulder boulder,
            BoulderColor color,
            String gradeFont,
            String gradeVScale,
            com.sendlt.domain.enums.HoldStyle holdStyle,
            java.time.LocalDate openedAt) {
        boulder.setColor(color);
        boulder.setGradeFont(normalize(gradeFont));
        boulder.setGradeVScale(normalize(gradeVScale));
        boulder.setHoldStyle(holdStyle);
        boulder.setOpenedAt(openedAt);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean matchesGradeFilters(
            Boulder boulder,
            String gradeFontMin,
            String gradeFontMax,
            String gradeVScaleMin,
            String gradeVScaleMax) {

        boolean fontFilter = gradeFontMin != null || gradeFontMax != null;
        boolean vScaleFilter = gradeVScaleMin != null || gradeVScaleMax != null;

        if (fontFilter && !FontGradeOrder.isInRange(boulder.getGradeFont(), gradeFontMin, gradeFontMax)) {
            return false;
        }
        if (vScaleFilter && !VScaleOrder.isInRange(boulder.getGradeVScale(), gradeVScaleMin, gradeVScaleMax)) {
            return false;
        }
        return true;
    }
}
