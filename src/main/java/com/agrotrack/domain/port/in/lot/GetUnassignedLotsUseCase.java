package com.agrotrack.domain.port.in.lot;

import com.agrotrack.domain.model.entities.Lot;
import java.util.List;
import java.util.UUID;

public interface GetUnassignedLotsUseCase {
    List<Lot> executeGetUnassignedLots(UUID userId);
}
