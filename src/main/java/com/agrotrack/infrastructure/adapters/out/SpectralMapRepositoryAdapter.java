package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SpectralMapRepositoryAdapter implements SpectralMapRepositoryPort {

    @Override
    public SpectralMap save(SpectralMap spectralMap) {
        return spectralMap;
    }

    @Override
    public Optional<SpectralMap> findById(UUID mapId) {
        return Optional.empty();
    }

    @Override
    public List<SpectralMap> findByLotId(UUID lotId) {
        return List.of();
    }
}