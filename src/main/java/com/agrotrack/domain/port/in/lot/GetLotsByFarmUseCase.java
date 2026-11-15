package com.agrotrack.domain.port.in.lot;

import com.agrotrack.application.dto.LotDto;
import java.util.List;
import java.util.UUID;

public interface GetLotsByFarmUseCase {
    List<LotDto> executeGetLotsByFarm(UUID farmId);
}
