package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.LotJpaRepository;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class LotRepositoryAdapter implements LotRepositoryPort {

    private final LotJpaRepository lotJpaRepository;
    private final FarmRepositoryPort farmRepositoryPort;


    public LotRepositoryAdapter(LotJpaRepository lotJpaRepository, FarmRepositoryPort farmRepositoryPort) {
        this.lotJpaRepository = lotJpaRepository;
        this.farmRepositoryPort = farmRepositoryPort;
    }

    @Override
    public Lot save(Lot lot) {
        LotJpaEntity entity = new LotJpaEntity();
        entity.setId(lot.getIdLot());
        entity.setName(lot.getName());
        entity.setHectares(lot.getHectares());
        entity.setSoilType(lot.getSoilType());
        entity.setType(lot.getType());
        entity.setDescription(lot.getDescription());
        entity.setPolygonLimit(lot.getPolygonLimit());
        entity.setState(lot.getState());
        entity.setActive(lot.isActive());
        entity.setDeletionReason(lot.getDeletionReason());
        // Mapear la Farm al JPA Entity si existe
        if (lot.getFarm() != null) {
            FarmJpaEntity farmEntity = new FarmJpaEntity();
            farmEntity.setId(lot.getFarm().getIdFarm());
            entity.setFarm(farmEntity);
        }

        LotJpaEntity savedEntity = lotJpaRepository.save(entity);
        lot.setIdLot(savedEntity.getId());
        return lot;
    }

    @Override
    public Optional<Lot> findById(UUID id) {
        return lotJpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Lot> findByFarmId(UUID farmId) {
        return lotJpaRepository.findByFarmId(farmId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Lot> findUnassignedLotsByUserId(UUID userId) {
        return lotJpaRepository.findUnassignedLotsForUser(userId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsOverlappingLot(Polygon newPerimeter, UUID excludeLotId) {
        return lotJpaRepository.existsOverlappingLot(newPerimeter, excludeLotId);
    }

    private Lot mapToDomain(LotJpaEntity entity) {
        Lot lot = Lot.create(
                entity.getName(), entity.getHectares(), entity.getSoilType(),
                entity.getType(), entity.getDescription(), entity.getPolygonLimit(), farmRepositoryPort.findById(entity.getFarm().getId()).orElse(null)
        );
        lot.setIdLot(entity.getId());
        lot.changeState(entity.getState());
        lot.setActive(entity.isActive());
        lot.setDeletionReason(entity.getDeletionReason());
        return lot;
    }
}