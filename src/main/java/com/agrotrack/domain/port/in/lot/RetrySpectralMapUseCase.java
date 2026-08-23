package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.SpectralMap;
import java.util.UUID;

public interface RetrySpectralMapUseCase {
    SpectralMap executeRetryProcessing(UUID mapId);
}
