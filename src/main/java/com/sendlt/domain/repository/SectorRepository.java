package com.sendlt.domain.repository;

import com.sendlt.domain.entity.Sector;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, UUID> {

    List<Sector> findByGymIdOrderByNameAsc(UUID gymId);
}
