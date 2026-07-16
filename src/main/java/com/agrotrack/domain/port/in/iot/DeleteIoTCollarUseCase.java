package com.agrotrack.domain.port.in.iot;

import java.util.UUID;

public interface DeleteIoTCollarUseCase {
    void executeDeleteIoTCollar(UUID collarId);
}
