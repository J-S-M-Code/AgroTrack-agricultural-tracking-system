package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.entities.Crop;

import java.util.UUID;

public interface GetCropByIdUseCase {
    Crop executeGetCropById(UUID cropId);
}
