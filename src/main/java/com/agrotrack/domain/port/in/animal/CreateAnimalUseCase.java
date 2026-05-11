package com.agrotrack.domain.port.in.animal;

import com.agrotrack.domain.model.entities.Animal;

public interface CreateAnimalUseCase {
    Animal execute(Animal animal);
}
