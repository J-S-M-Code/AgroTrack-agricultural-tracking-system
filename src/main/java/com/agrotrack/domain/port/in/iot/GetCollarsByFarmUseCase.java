package com.agrotrack.domain.port.in.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import java.util.List;
import java.util.UUID;

public interface GetCollarsByFarmUseCase {
    List<IoTCollar> executeGetCollarsByFarm(UUID farmId);
}
