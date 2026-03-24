package com.agrotrack.domain.port.out.field;

import com.agrotrack.domain.model.entities.SpectralMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpectralMapRepositoryPort {
    SpectralMap save(SpectralMap spectralMap);
    Optional<SpectralMap> findById(UUID spectralMapId);
    List<SpectralMap> findByFieldId(UUID fieldId);
}
