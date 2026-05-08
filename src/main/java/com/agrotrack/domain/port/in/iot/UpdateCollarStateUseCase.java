package com.agrotrack.domain.port.in.iot;

import com.agrotrack.domain.model.enums.State;
import java.util.UUID;

public interface UpdateCollarStateUseCase {
    void execute(UUID collarId, State newState);
}