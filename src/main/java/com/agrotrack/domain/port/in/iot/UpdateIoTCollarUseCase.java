package com.agrotrack.domain.port.in.iot;

import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.enums.State;
import java.util.UUID;

public interface UpdateIoTCollarUseCase {
    IoTCollar executeUpdateIoTCollar(UUID collarId, String codeRFID, String model, State state);
}
