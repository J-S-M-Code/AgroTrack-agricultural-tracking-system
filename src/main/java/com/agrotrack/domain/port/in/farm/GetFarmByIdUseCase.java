package com.agrotrack.domain.port.in.farm;

import com.agrotrack.application.dto.FarmDto;
import java.util.UUID;

public interface GetFarmByIdUseCase {
    FarmDto executeGetFarmById(UUID farmId);
}
