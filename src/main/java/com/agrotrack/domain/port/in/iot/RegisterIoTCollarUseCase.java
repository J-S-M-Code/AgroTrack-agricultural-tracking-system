package com.agrotrack.domain.port.in.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;

public interface RegisterIoTCollarUseCase {
    IoTCollar executeRegisterIoTCollar(String codeRFID, String model, State state, Double batteryLevel);
}