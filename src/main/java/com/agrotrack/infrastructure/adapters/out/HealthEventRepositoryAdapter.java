package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.HealthEvent;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.animal.HealthEventRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.HealthEventJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.HealthEventJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class HealthEventRepositoryAdapter implements HealthEventRepositoryPort {

    private final HealthEventJpaRepository healthEventJpaRepository;

    public HealthEventRepositoryAdapter(HealthEventJpaRepository healthEventJpaRepository) {
        this.healthEventJpaRepository = healthEventJpaRepository;
    }

    @Override
    public HealthEvent save(HealthEvent healthEvent) {
        UserJpaEntity vetEntity = new UserJpaEntity();
        vetEntity.setId(healthEvent.getVeterinarian().getIdUser());

        HealthEventJpaEntity entity = new HealthEventJpaEntity(
                healthEvent.getIdHealthEvent(),
                healthEvent.getDate(),
                healthEvent.getType(),
                healthEvent.getTreatment(),
                healthEvent.getNumAct(),
                healthEvent.getObservation(),
                vetEntity
        );

        HealthEventJpaEntity savedEntity = healthEventJpaRepository.save(entity);
        healthEvent.setIdHealthEvent(savedEntity.getId());
        return healthEvent;
    }

    @Override
    public List<HealthEvent> findByAnimalId(UUID animalId) {
        return healthEventJpaRepository.findByAnimalId(animalId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private HealthEvent mapToDomain(HealthEventJpaEntity entity) {
        User vet = User.create(
                entity.getVeterinarian().getName(), entity.getVeterinarian().getLastName(),
                entity.getVeterinarian().getDni(), entity.getVeterinarian().getPhone(),
                entity.getVeterinarian().getAddress(), entity.getVeterinarian().getEmail(),
                new Password("Dummy@2026")
        );
        vet.setIdUser(entity.getVeterinarian().getId());

        HealthEvent event = HealthEvent.create(
                entity.getDate(), entity.getType(), entity.getTreatment(),
                entity.getNumAct(), entity.getObservation(), vet
        );
        event.setIdHealthEvent(entity.getId());
        return event;
    }
}