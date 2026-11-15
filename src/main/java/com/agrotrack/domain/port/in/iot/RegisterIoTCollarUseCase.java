package com.agrotrack.domain.port.in.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;

import java.util.UUID;

public interface RegisterIoTCollarUseCase {
    IoTCollar executeRegisterIoTCollar(String codeRFID, String model, State state, Double batteryLevel, UUID farmId);
}