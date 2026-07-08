package com.agrotrack.domain.port.in.animal;

import com.agrotrack.domain.model.entities.Animal;

import java.util.List;
import java.util.UUID;

public interface GetAnimalsByFarmUseCase {
    List<Animal> executeGetAnimalsByFarm(UUID farmId);
}
