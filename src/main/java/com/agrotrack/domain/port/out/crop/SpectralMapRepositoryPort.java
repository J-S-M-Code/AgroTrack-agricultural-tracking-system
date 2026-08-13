package com.agrotrack.domain.port.out.crop;

import com.agrotrack.domain.model.entities.SpectralMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpectralMapRepositoryPort {
    SpectralMap save(SpectralMap spectralMap);
    Optional<SpectralMap> findById(UUID mapId);

    // Obtener todo el historial de mapas de una finca específica
    List<SpectralMap> findByFarmId(UUID farmId);

    void delete(UUID mapId);
}