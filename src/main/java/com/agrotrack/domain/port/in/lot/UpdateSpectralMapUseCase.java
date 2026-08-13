package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.SpectralMap;
import java.util.UUID;

public interface UpdateSpectralMapUseCase {
    SpectralMap executeUpdateSpectralMap(
            UUID mapId,
            Double cloudCoverPercentage, 
            Double resolutionGSD, 
            Double meanIndexValue,
            String description
    );
}
