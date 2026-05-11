package com.agrotrack.domain.port.in.field;

import com.agrotrack.domain.model.entities.SpectralMap;

import java.time.LocalDateTime;

public interface ProcessSpectralMapUseCase {
    SpectralMap execute(SpectralMap spectralMapData, byte[] imageBytes);
}
