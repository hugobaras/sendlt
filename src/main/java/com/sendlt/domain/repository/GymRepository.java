package com.sendlt.domain.repository;

import com.sendlt.domain.entity.Gym;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, UUID> {}
