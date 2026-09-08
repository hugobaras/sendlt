package com.sendlt.application.service;

import com.sendlt.api.dto.sector.CreateSectorRequest;
import com.sendlt.api.dto.sector.SectorResponse;
import com.sendlt.api.dto.sector.UpdateSectorRequest;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.entity.Sector;
import com.sendlt.domain.repository.SectorRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;
    private final GymService gymService;
    private final DomainMapper mapper;

    @Transactional(readOnly = true)
    public List<SectorResponse> findByGym(UUID gymId) {
        gymService.findById(gymId);
        return sectorRepository.findByGymIdOrderByNameAsc(gymId).stream()
                .map(mapper::toSectorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectorResponse findById(UUID id) {
        return mapper.toSectorResponse(getSector(id));
    }

    @Transactional
    public SectorResponse create(CreateSectorRequest request) {
        Gym gym = gymService.getGym(request.gymId());
        Sector sector = new Sector();
        sector.setGym(gym);
        sector.setName(request.name().trim());
        return mapper.toSectorResponse(sectorRepository.save(sector));
    }

    @Transactional
    public SectorResponse update(UUID id, UpdateSectorRequest request) {
        Sector sector = getSector(id);
        sector.setName(request.name().trim());
        return mapper.toSectorResponse(sectorRepository.save(sector));
    }

    @Transactional
    public void delete(UUID id) {
        if (!sectorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sector", id);
        }
        sectorRepository.deleteById(id);
    }

    Sector getSector(UUID id) {
        return sectorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector", id));
    }
}
