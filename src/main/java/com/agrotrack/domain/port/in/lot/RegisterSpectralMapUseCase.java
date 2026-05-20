package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterSpectralMapUseCase {
    // El servicio buscará el Lot a través del assignedLotId antes de crear la entidad
    SpectralMap executeRegisterSpectralMap(String urlSpectralMap, LocalDateTime flightDate, SpectralMapType indexType,
                        Double cloudCoverPercentage, Double resolutionGSD, Double meanIndexValue,
                        UUID assignedLotId);
}