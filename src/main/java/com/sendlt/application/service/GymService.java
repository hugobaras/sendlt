package com.sendlt.application.service;

import com.sendlt.api.dto.gym.CreateGymRequest;
import com.sendlt.api.dto.gym.GymResponse;
import com.sendlt.api.dto.gym.UpdateGymRequest;
import com.sendlt.api.mapper.DomainMapper;
import com.sendlt.application.exception.ResourceNotFoundException;
import com.sendlt.domain.entity.Gym;
import com.sendlt.domain.repository.GymRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final DomainMapper mapper;

    @Transactional(readOnly = true)
    public List<GymResponse> findAll() {
        return gymRepository.findAll().stream().map(mapper::toGymResponse).toList();
    }

    @Transactional(readOnly = true)
    public GymResponse findById(UUID id) {
        return mapper.toGymResponse(getGym(id));
    }

    @Transactional
    public GymResponse create(CreateGymRequest request) {
        Gym gym = new Gym();
        gym.setName(request.name().trim());
        gym.setCity(request.city());
        gym.setScoringMode(request.scoringMode());
        return mapper.toGymResponse(gymRepository.save(gym));
    }

    @Transactional
    public GymResponse update(UUID id, UpdateGymRequest request) {
        Gym gym = getGym(id);
        gym.setName(request.name().trim());
        gym.setCity(request.city());
        gym.setScoringMode(request.scoringMode());
        return mapper.toGymResponse(gymRepository.save(gym));
    }

    @Transactional
    public void delete(UUID id) {
        if (!gymRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gym", id);
        }
        gymRepository.deleteById(id);
    }

    Gym getGym(UUID id) {
        return gymRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Gym", id));
    }
}
