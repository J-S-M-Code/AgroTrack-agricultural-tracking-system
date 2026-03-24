package com.agrotrack.domain.port.in.animal;

import java.util.UUID;

public interface MoveAnimalToFieldUseCase {
    void execute(UUID animal, UUID destinationField, String movementReason);
}
