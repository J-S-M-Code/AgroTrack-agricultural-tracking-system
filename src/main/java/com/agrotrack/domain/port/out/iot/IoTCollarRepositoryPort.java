package com.agrotrack.domain.port.out.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IoTCollarRepositoryPort {
    IoTCollar save(IoTCollar collar);
    Optional<IoTCollar> findById(UUID id);
    Optional<IoTCollar> findByCodeRFID(String codeRFID);
    List<IoTCollar> findByFarmId(UUID farmId);
    List<IoTCollar> findByState(State state);
    void delete(UUID id);

    // Un método muy útil para disparar alertas preventivas de mantenimiento
    List<IoTCollar> findByBatteryLevelLessThan(Double batteryThreshold);
}