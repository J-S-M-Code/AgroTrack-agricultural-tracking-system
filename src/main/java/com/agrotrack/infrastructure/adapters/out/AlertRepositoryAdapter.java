package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AlertRepositoryAdapter implements AlertRepositoryPort {
    @Override public Alert save(Alert alert) { return alert; }
    @Override public Optional<Alert> findById(UUID alertId) { return Optional.empty(); }
    @Override public List<Alert> findByLotId(UUID lotId) { return List.of(); }
    @Override public List<Alert> findActiveAlertsByPriority(Priority priority) { return List.of(); }
}