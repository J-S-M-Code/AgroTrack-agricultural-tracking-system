package com.agrotrack.domain.port.in.alert;

import java.util.UUID;

public interface DeleteAlertUseCase {
    void executeDeleteAlert(UUID alertId);
}
