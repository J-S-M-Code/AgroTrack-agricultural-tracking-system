package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.entities.Crop;

public interface RegisterCropUseCase {
    Crop execute(Crop crop);
}
