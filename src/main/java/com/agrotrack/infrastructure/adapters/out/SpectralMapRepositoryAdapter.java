package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
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

    public SpectralMapRepositoryAdapter(SpectralMapJpaRepository spectralMapJpaRepository) {
        this.spectralMapJpaRepository = spectralMapJpaRepository;
    }

    @Override
    public SpectralMap save(SpectralMap spectralMap) {
        LotJpaEntity lotEntity = new LotJpaEntity();
        lotEntity.setId(spectralMap.getAssignedLot().getIdLot());

        SpectralMapJpaEntity entity = new SpectralMapJpaEntity(
                spectralMap.getIdMap(),
                spectralMap.getUrlSpectralMap(),
                spectralMap.getFlightDate(),
                spectralMap.getIndexType(),
                spectralMap.getCloudCoverPercentage(),
                spectralMap.getResolutionGSD(),
                spectralMap.getMeanIndexValue(),
                lotEntity
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
    public List<SpectralMap> findByLotId(UUID lotId) {
        return spectralMapJpaRepository.findByLotId(lotId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private SpectralMap mapToDomain(SpectralMapJpaEntity entity) {
        Lot lot = Lot.create(
                entity.getLot().getName(), entity.getLot().getHectares(), entity.getLot().getSoilType(),
                entity.getLot().getType(), entity.getLot().getDescription(), entity.getLot().getPolygonLimit()
        );
        lot.setIdLot(entity.getLot().getId());

        SpectralMap map = SpectralMap.create(
                entity.getUrlSpectralMap(), entity.getFlightDate(), entity.getIndexType(),
                entity.getCloudCoverPercentage(), entity.getResolutionGSD(),
                entity.getMeanIndexValue(), lot
        );
        map.setIdMap(entity.getId());
        return map;
    }
}