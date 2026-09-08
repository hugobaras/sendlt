package com.sendlt.domain.repository;

import com.sendlt.domain.entity.Boulder;
import com.sendlt.domain.enums.BoulderColor;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoulderRepository extends JpaRepository<Boulder, UUID> {

    List<Boulder> findBySectorIdOrderByOpenedAtDesc(UUID sectorId);

    List<Boulder> findBySectorIdAndColorOrderByOpenedAtDesc(UUID sectorId, BoulderColor color);
}
