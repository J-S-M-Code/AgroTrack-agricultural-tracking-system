package com.agrotrack.domain.port.in.alert;

import com.agrotrack.domain.model.entities.Alert;

import java.util.List;
import java.util.UUID;

public interface GetAlertsByFarmUseCase {
    List<Alert> executeGetAlertsByFarm(UUID farmId);
}
