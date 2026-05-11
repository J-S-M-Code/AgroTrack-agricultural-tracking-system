package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SpectralMapService implements RegisterSpectralMapUseCase {

    private final SpectralMapRepositoryPort spectralMapRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;

    public SpectralMapService(SpectralMapRepositoryPort spectralMapRepositoryPort, LotRepositoryPort lotRepositoryPort) {
        this.spectralMapRepositoryPort = spectralMapRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
    }

    @Override
    @Transactional
    public SpectralMap executeRegisterSpectralMap(String urlSpectralMap, LocalDateTime flightDate, SpectralMapType indexType,
                               Double cloudCoverPercentage, Double resolutionGSD, Double meanIndexValue,
                               UUID assignedLotId) {

        // 1. Validar que el Lote exista
        Lot lot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + assignedLotId));

        // 2. Instanciar el mapa espectral a través de su factory method
        // (Las validaciones de la fecha límite y del porcentaje de nubosidad ocurren dentro)
        SpectralMap newMap = SpectralMap.create(
                urlSpectralMap,
                flightDate,
                indexType,
                cloudCoverPercentage,
                resolutionGSD,
                meanIndexValue,
                lot
        );

        // 3. Vincular el mapa a la lista del lote
        lot.addSpectralMap(newMap);

        // 4. Guardar los cambios
        // Guardamos el mapa (y opcionalmente el lote si la persistencia JPA lo requiere por relaciones bidireccionales)
        lotRepositoryPort.save(lot);
        return spectralMapRepositoryPort.save(newMap);
    }
}