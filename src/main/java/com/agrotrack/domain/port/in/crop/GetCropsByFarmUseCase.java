package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.entities.Crop;

import java.util.List;
import java.util.UUID;

public interface GetCropsByFarmUseCase {
    List<Crop> executeGetCropsByFarm(UUID farmId);
}
