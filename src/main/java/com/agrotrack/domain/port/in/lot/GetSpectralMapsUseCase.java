package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.SpectralMap;
import java.util.List;
import java.util.UUID;

public interface GetSpectralMapsUseCase {
    List<SpectralMap> executeGetSpectralMapsByFarm(UUID farmId);
}
