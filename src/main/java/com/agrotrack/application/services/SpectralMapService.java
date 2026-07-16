package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SpectralMapService implements RegisterSpectralMapUseCase {

    private final SpectralMapRepositoryPort spectralMapRepositoryPort;
    private final AsyncMapProcessor asyncMapProcessor;
    private final LotRepositoryPort lotRepositoryPort;


    public SpectralMapService(SpectralMapRepositoryPort spectralMapRepositoryPort,
                            AsyncMapProcessor asyncMapProcessor,
                            LotRepositoryPort lotRepositoryPort) {
        this.spectralMapRepositoryPort = spectralMapRepositoryPort;
        this.asyncMapProcessor = asyncMapProcessor;
        this.lotRepositoryPort = lotRepositoryPort;
    }

    @Override
    @Transactional
    public SpectralMap executeRegisterSpectralMap(
            String minioRawPath, 
            LocalDateTime flightDate, 
            SpectralMapType indexType,
            Double cloudCoverPercentage, 
            Double resolutionGSD, 
            Double meanIndexValue,
            UUID assignedLotId) {

        // Definimos el lote ya que vamos a utilizar los datos para asignar y guardar los datos
        Lot mapLot = lotRepositoryPort.findById(assignedLotId)
            .orElseThrow(() -> new IllegalArgumentException("Problemas al asignar el lote al mapeo"));

        // 1. Instanciar el objeto de dominio con todos los parámetros definidos
        SpectralMap map = SpectralMap.create(
            minioRawPath, // URL interna proveniente del frontend
            flightDate,
            indexType,
            cloudCoverPercentage,
            resolutionGSD,
            meanIndexValue,
            mapLot
        );


        // 2. Persistir para obtener el ID autogenerado
        SpectralMap savedMap = spectralMapRepositoryPort.save(map);

        // 3. Disparar el motor de paches en segundo plano
        asyncMapProcessor.processAndTileMap(savedMap, minioRawPath, indexType);

        // 4. Retornar de inmediato el mapa en estado PENDING para no bloquear la API
        return savedMap;
    }
}