package com.agrotrack.domain.port.out.crop;

import com.agrotrack.domain.model.entities.Crop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropRepositoryPort {
    Crop save(Crop crop);
    Optional<Crop> findById(UUID cropId);

    // Útil para saber qué está plantado actualmente en un lote
    List<Crop> findActiveCropsByLotId(UUID lotId);

    List<Crop> findByFarmId(UUID farmId);
    List<Crop> findUnassignedCropsByUserId(UUID userId);
}