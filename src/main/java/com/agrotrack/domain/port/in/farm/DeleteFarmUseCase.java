package com.agrotrack.domain.port.in.farm;

import java.util.UUID;

public interface DeleteFarmUseCase {
    void executeDeleteFarm(UUID farmId, String reason);
}
