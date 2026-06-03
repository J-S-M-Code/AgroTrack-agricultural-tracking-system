package com.agrotrack.domain.port.out;

import com.agrotrack.domain.model.entities.Lot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotRepositoryPort {
    Lot save(Lot lot);
    Optional<Lot> findById(UUID id);
    List<Lot> findByFarmId(UUID farmId);
}
