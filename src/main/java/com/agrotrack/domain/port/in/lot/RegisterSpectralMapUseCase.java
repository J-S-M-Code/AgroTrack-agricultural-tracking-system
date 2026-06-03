package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterSpectralMapUseCase {
    SpectralMap executeRegisterSpectralMap(
            InputStream geoTiffStream, 
            LocalDateTime flightDate, 
            SpectralMapType indexType,
            Double cloudCoverPercentage, 
            Double resolutionGSD, 
            Double meanIndexValue,
            UUID assignedLotId
    );
}