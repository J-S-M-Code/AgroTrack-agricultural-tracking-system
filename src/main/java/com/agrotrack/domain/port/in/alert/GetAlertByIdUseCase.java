package com.agrotrack.domain.port.in.alert;

import com.agrotrack.domain.model.entities.Alert;

import java.util.UUID;

public interface GetAlertByIdUseCase {
    Alert executeGetAlertById(UUID alertId);
}
