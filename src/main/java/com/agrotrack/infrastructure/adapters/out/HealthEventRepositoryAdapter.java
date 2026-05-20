package com.agrotrack.infrastructure.adapters.out;
import com.agrotrack.domain.model.entities.HealthEvent;
import com.agrotrack.domain.port.out.animal.HealthEventRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public class HealthEventRepositoryAdapter implements HealthEventRepositoryPort {
    @Override public HealthEvent save(HealthEvent healthEvent) { return healthEvent; }
    @Override public List<HealthEvent> findByAnimalId(UUID animalId) { return List.of(); }
}