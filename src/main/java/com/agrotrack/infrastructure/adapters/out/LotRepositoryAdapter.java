package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
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

    public LotRepositoryAdapter(LotJpaRepository lotJpaRepository) {
        this.lotJpaRepository = lotJpaRepository;
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
    public boolean existsOverlappingLot(Polygon newPerimeter, UUID excludeLotId) {
        return lotJpaRepository.existsOverlappingLot(newPerimeter, excludeLotId);
    }

    private Lot mapToDomain(LotJpaEntity entity) {
        Lot lot = Lot.create(
                entity.getName(), entity.getHectares(), entity.getSoilType(),
                entity.getType(), entity.getDescription(), entity.getPolygonLimit()
        );
        lot.setIdLot(entity.getId());
        lot.changeState(entity.getState());
        return lot;
    }
}