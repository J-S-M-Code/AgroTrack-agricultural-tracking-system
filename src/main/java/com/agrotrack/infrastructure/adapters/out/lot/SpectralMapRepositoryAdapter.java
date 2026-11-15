package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.SpectralMapJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.SpectralMapJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SpectralMapRepositoryAdapter implements SpectralMapRepositoryPort {

    private final SpectralMapJpaRepository spectralMapJpaRepository;
    private final FarmRepositoryPort farmRepositoryPort;

    public SpectralMapRepositoryAdapter(SpectralMapJpaRepository spectralMapJpaRepository, FarmRepositoryPort farmRepositoryPort) {
        this.spectralMapJpaRepository = spectralMapJpaRepository;
        this.farmRepositoryPort = farmRepositoryPort;
    }

    @Override
    public SpectralMap save(SpectralMap spectralMap) {

        com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity farmEntity = new com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity();
        farmEntity.setId(spectralMap.getFarmId());

        SpectralMapJpaEntity entity = new SpectralMapJpaEntity(
                spectralMap.getIdMap(),
                spectralMap.getUrlSpectralMap(),
                spectralMap.getFlightDate(),
                spectralMap.getIndexType(),
                spectralMap.getCloudCoverPercentage(),
                spectralMap.getResolutionGSD(),
                spectralMap.getMeanIndexValue(),
                spectralMap.getTilesBaseUrl(),
                spectralMap.getDescription(),
                spectralMap.getMapStatus(),

                farmEntity
        );

        SpectralMapJpaEntity savedEntity = spectralMapJpaRepository.save(entity);
        spectralMap.setIdMap(savedEntity.getId());
        return spectralMap;
    }

    @Override
    public Optional<SpectralMap> findById(UUID mapId) {
        return spectralMapJpaRepository.findById(mapId).map(this::mapToDomain);
    }

    @Override
    public List<SpectralMap> findByFarmId(UUID farmId) {
        return spectralMapJpaRepository.findByFarmId(farmId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID mapId) {
        spectralMapJpaRepository.deleteById(mapId);
    }

    private SpectralMap mapToDomain(SpectralMapJpaEntity entity) {
        SpectralMap map = SpectralMap.create(
                entity.getUrlSpectralMap(), entity.getFlightDate(), entity.getIndexType(),
                entity.getCloudCoverPercentage(), entity.getResolutionGSD(),
                entity.getMeanIndexValue(), entity.getFarm().getId(), entity.getDescription()
        );
        map.setIdMap(entity.getId());
        map.setTilesBaseUrl(entity.getTilesBaseUrl());
        map.setMapStatus(entity.getMapStatus() != null ? entity.getMapStatus() : com.agrotrack.domain.model.enums.MapStatus.PENDING);
        return map;
    }
}