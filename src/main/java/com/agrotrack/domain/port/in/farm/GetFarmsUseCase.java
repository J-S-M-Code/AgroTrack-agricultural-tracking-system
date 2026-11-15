package com.agrotrack.domain.port.in.farm;

import com.agrotrack.application.dto.FarmDto;
import java.util.List;
import java.util.UUID;

public interface GetFarmsUseCase {
    List<FarmDto> executeGetFarmsByUser(UUID userId);
}
