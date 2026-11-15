package com.agrotrack.domain.port.in.user;

import com.agrotrack.application.dto.FarmContextDto;
import java.util.UUID;

public interface GetFarmContextUseCase {
    FarmContextDto executeGetFarmContext(UUID userId, UUID farmId);
}
