package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.entities.Crop;
import java.util.List;
import java.util.UUID;

public interface GetUnassignedCropsUseCase {
    List<Crop> executeGetUnassignedCrops(UUID userId);
}
