package com.agrotrack.domain.port.in.animal;

import java.util.UUID;

public interface DeleteAnimalUseCase {
    void executeDeleteAnimal(UUID animalId, String reason);
}
