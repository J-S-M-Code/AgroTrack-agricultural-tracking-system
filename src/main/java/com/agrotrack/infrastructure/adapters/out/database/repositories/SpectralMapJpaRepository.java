package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.SpectralMapJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpectralMapJpaRepository extends JpaRepository<SpectralMapJpaEntity, UUID> {
    List<SpectralMapJpaEntity> findByFarmId(UUID farmId);
}