package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LotRepositoryAdapter implements LotRepositoryPort {

    @Override
    public Lot save(Lot lot) {
        return lot;
    }

    @Override
    public Optional<Lot> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Lot> findByFarmId(UUID farmId) {
        return List.of();
    }

    @Override
    public boolean existsOverlappingLot(Polygon newPerimeter, UUID excludeLotId) {
        return false;
    }
}