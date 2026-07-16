package com.agrotrack.domain.port.out.alert;

import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.enums.Priority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepositoryPort {
    Alert save(Alert alert);
    Optional<Alert> findById(UUID alertId);
    List<Alert> findByLotId(UUID lotId);
    List<Alert> findActiveAlertsByPriority(Priority priority);
    List<Alert> findByFarmId(UUID farmId);
}