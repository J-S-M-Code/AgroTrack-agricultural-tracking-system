package com.agrotrack.domain.port.in.iot;

import java.util.UUID;

public interface UpdateCollarBatteryUseCase {
    // Ideal para conectarlo a un Webhook o MQTT que reciba los pings del hardware
    void execute(UUID collarId, Double currentBatteryLevel);
}