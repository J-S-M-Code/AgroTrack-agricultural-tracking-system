package com.agrotrack.domain.port.in.user;

import java.util.UUID;

public interface RemovePersonnelFromFarmUseCase {
    void executeRemovePersonnelFromFarm(UUID farmId, UUID userId);
}
