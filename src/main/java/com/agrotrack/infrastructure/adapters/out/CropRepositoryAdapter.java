package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CropRepositoryAdapter implements CropRepositoryPort {

    @Override
    public Crop save(Crop crop) {
        return crop;
    }

    @Override
    public Optional<Crop> findById(UUID cropId) {
        return Optional.empty();
    }

    @Override
    public List<Crop> findActiveCropsByLotId(UUID lotId) {
        return List.of();
    }
}