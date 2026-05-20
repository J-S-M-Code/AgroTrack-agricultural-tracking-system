package com.agrotrack.infrastructure.adapters.out;
import com.agrotrack.domain.model.entities.AnimalMovement;
import com.agrotrack.domain.port.out.animal.AnimalMovementRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnimalMovementRepositoryAdapter implements AnimalMovementRepositoryPort {
    @Override public AnimalMovement save(AnimalMovement movement) { return movement; }
    @Override public Optional<AnimalMovement> findActiveMovementByAnimalId(UUID animalId) { return Optional.empty(); }
    @Override public List<AnimalMovement> findAllByAnimalId(UUID animalId) { return List.of(); }
}