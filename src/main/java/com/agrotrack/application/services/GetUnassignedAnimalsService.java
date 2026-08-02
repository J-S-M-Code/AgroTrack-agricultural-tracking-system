package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.port.in.animal.GetUnassignedAnimalsUseCase;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetUnassignedAnimalsService implements GetUnassignedAnimalsUseCase {

    private final AnimalRepositoryPort animalRepositoryPort;

    public GetUnassignedAnimalsService(AnimalRepositoryPort animalRepositoryPort) {
        this.animalRepositoryPort = animalRepositoryPort;
    }

    @Override
    public List<Animal> executeGetUnassignedAnimals(UUID userId) {
        return animalRepositoryPort.findUnassignedByUserId(userId);
    }
}
