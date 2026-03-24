package com.agrotrack.domain.port.in.animal;

import java.util.UUID;

public interface AssignRfidTagUseCase {
    void execute(UUID animalId, String rfidTag);
}
