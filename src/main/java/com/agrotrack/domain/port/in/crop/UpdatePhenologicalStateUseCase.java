package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.enums.PhenologicalState;
import java.util.UUID;

public interface UpdatePhenologicalStateUseCase {
    void executeUpdatePhenologicalState(UUID cropId, PhenologicalState newState);
}