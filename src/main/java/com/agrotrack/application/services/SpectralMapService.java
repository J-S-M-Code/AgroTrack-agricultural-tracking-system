package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.in.lot.DeleteSpectralMapUseCase;
import com.agrotrack.domain.port.in.lot.GeneratePresignedUrlUseCase;
import com.agrotrack.domain.port.in.lot.GetSpectralMapsUseCase;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
import com.agrotrack.domain.port.in.lot.UpdateSpectralMapUseCase;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SpectralMapService implements RegisterSpectralMapUseCase, GetSpectralMapsUseCase, DeleteSpectralMapUseCase, GeneratePresignedUrlUseCase, UpdateSpectralMapUseCase {

    private final SpectralMapRepositoryPort spectralMapRepositoryPort;
    private final AsyncMapProcessor asyncMapProcessor;
    private final FarmRepositoryPort farmRepositoryPort;
    private final FileStoragePort fileStoragePort;

    public SpectralMapService(SpectralMapRepositoryPort spectralMapRepositoryPort,
                            AsyncMapProcessor asyncMapProcessor,
                            FarmRepositoryPort farmRepositoryPort,
                            FileStoragePort fileStoragePort) {
        this.spectralMapRepositoryPort = spectralMapRepositoryPort;
        this.asyncMapProcessor = asyncMapProcessor;
        this.farmRepositoryPort = farmRepositoryPort;
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public SpectralMap executeUpdateSpectralMap(UUID mapId, Double cloudCoverPercentage, Double resolutionGSD, Double meanIndexValue, String description) {
        SpectralMap map = spectralMapRepositoryPort.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Mapa no encontrado"));
                
        if (cloudCoverPercentage != null && (cloudCoverPercentage < 0 || cloudCoverPercentage > 100)) {
            throw new IllegalArgumentException("El porcentaje de nubosidad debe estar entre 0 y 100");
        }
        if (resolutionGSD != null && resolutionGSD <= 0) {
            throw new IllegalArgumentException("La resolución GSD debe ser un valor positivo");
        }
                
        map.setCloudCoverPercentage(cloudCoverPercentage);
        map.setResolutionGSD(resolutionGSD);
        map.setMeanIndexValue(meanIndexValue);
        map.setDescription(description);
        
        return spectralMapRepositoryPort.save(map);
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
            UUID assignedFarmId,
            String description) {

        // Definimos la finca ya que vamos a utilizar los datos para asignar y guardar los datos
        if (!farmRepositoryPort.findById(assignedFarmId).isPresent()) {
            throw new IllegalArgumentException("Problemas al asignar la finca al mapeo");
        }

        // 1. Instanciar el objeto de dominio con todos los parámetros definidos
        SpectralMap map = SpectralMap.create(
            minioRawPath, // URL interna proveniente del frontend
            flightDate,
            indexType,
            cloudCoverPercentage,
            resolutionGSD,
            meanIndexValue,
            assignedFarmId,
            description
        );


        // 2. Persistir para obtener el ID autogenerado
        SpectralMap savedMap = spectralMapRepositoryPort.save(map);

        // 3. Disparar el motor de parches en segundo plano UNA VEZ que se haya hecho commit en la base de datos
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncMapProcessor.processAndTileMap(savedMap, minioRawPath, indexType);
            }
        });

        // 4. Retornar de inmediato el mapa en estado PENDING para no bloquear la API
        return savedMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpectralMap> executeGetSpectralMapsByFarm(UUID farmId) {
        return spectralMapRepositoryPort.findByFarmId(farmId);
    }

    @Override
    @Transactional
    public void executeDeleteSpectralMap(UUID mapId) {
        SpectralMap map = spectralMapRepositoryPort.findById(mapId)
            .orElseThrow(() -> new IllegalArgumentException("Mapa no encontrado"));
            
        // Extraemos las rutas antes de borrar el mapa de la base de datos
        String minioRawPath = map.getUrlSpectralMap();
        String objectName = minioRawPath.substring(minioRawPath.indexOf("mapas-espectrales"));
        String tilesBaseUrl = map.getTilesBaseUrl();
        
        // Eliminamos el mapa de la base de datos inmediatamente
        spectralMapRepositoryPort.delete(mapId);

        // Eliminamos los archivos de MinIO en un hilo en segundo plano para no bloquear al usuario
        // ya que la carpeta de tiles puede contener miles de archivos.
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                System.out.println(">>> Iniciando eliminación asíncrona de archivos en MinIO para el mapa: " + mapId);
                // Delete original TIF
                fileStoragePort.deleteFile(objectName);
                
                // Delete tiles directory
                if (tilesBaseUrl != null) {
                    fileStoragePort.deleteDirectory(tilesBaseUrl);
                }
                System.out.println(">>> Archivos eliminados de MinIO exitosamente para el mapa: " + mapId);
            } catch (Exception e) {
                System.err.println(">>> Error eliminando archivos de MinIO para el mapa " + mapId + ": " + e.getMessage());
            }
        });
    }

    @Override
    public String executeGeneratePresignedUrl(UUID farmId, String fileName) {
        if (fileName == null || (!fileName.toLowerCase().endsWith(".tif") && !fileName.toLowerCase().endsWith(".tiff"))) {
            throw new IllegalArgumentException("El archivo debe tener extensión .tif o .tiff para ser procesado.");
        }
        return fileStoragePort.generatePresignedUploadUrl("mapas-espectrales/crudos/" + farmId + "/" + UUID.randomUUID() + "-" + fileName);
    }
}