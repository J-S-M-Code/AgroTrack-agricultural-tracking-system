package com.agrotrack.domain.port.out.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IoTCollarRepositoryPort {
    IoTCollar save(IoTCollar collar);
    Optional<IoTCollar> findById(UUID collarId);
    Optional<IoTCollar> findByCodeRFID(String codeRFID);
    List<IoTCollar> findByState(State state);

    // Un método muy útil para disparar alertas preventivas de mantenimiento
    List<IoTCollar> findByBatteryLevelLessThan(Double batteryThreshold);
}