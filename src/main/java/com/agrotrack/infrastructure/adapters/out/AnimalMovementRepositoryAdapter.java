package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.AnimalMovement;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.out.animal.AnimalMovementRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalMovementJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.AnimalMovementJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AnimalMovementRepositoryAdapter implements AnimalMovementRepositoryPort {

    private final AnimalMovementJpaRepository animalMovementJpaRepository;
    private final FarmRepositoryPort farmRepositoryPort;

    public AnimalMovementRepositoryAdapter(AnimalMovementJpaRepository animalMovementJpaRepository, FarmRepositoryPort farmRepositoryPort) {
        this.animalMovementJpaRepository = animalMovementJpaRepository;
        this.farmRepositoryPort = farmRepositoryPort;
    }

    @Override
    public AnimalMovement save(AnimalMovement movement) {
        LotJpaEntity lotEntity = new LotJpaEntity();
        lotEntity.setId(movement.getOriginLot().getIdLot());

        UserJpaEntity userEntity = new UserJpaEntity();
        userEntity.setId(movement.getRegisteredBy().getIdUser());

        AnimalMovementJpaEntity entity = new AnimalMovementJpaEntity(
                movement.getIdMovement(),
                lotEntity,
                movement.getEntryDate(),
                movement.getExitDate(),
                userEntity
        );

        AnimalMovementJpaEntity savedEntity = animalMovementJpaRepository.save(entity);
        movement.setIdMovement(savedEntity.getId());
        return movement;
    }

    @Override
    public Optional<AnimalMovement> findActiveMovementByAnimalId(UUID animalId) {
        return animalMovementJpaRepository.findActiveMovementByAnimalId(animalId).map(this::mapToDomain);
    }

    @Override
    public List<AnimalMovement> findAllByAnimalId(UUID animalId) {
        return animalMovementJpaRepository.findAllByAnimalId(animalId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private AnimalMovement mapToDomain(AnimalMovementJpaEntity entity) {
        Lot lot = Lot.create(
                entity.getOriginLot().getName(), entity.getOriginLot().getHectares(),
                entity.getOriginLot().getSoilType(), entity.getOriginLot().getType(),
                entity.getOriginLot().getDescription(), entity.getOriginLot().getPolygonLimit(), farmRepositoryPort.findById(entity.getOriginLot().getFarm().getId()).orElse(null)
        );
        lot.setIdLot(entity.getOriginLot().getId());

        User user = User.create(
                entity.getRegisteredBy().getName(), entity.getRegisteredBy().getLastName(),
                entity.getRegisteredBy().getDni(), entity.getRegisteredBy().getPhone(),
                entity.getRegisteredBy().getAddress(), entity.getRegisteredBy().getEmail(),
                new Password("DUMMY123*"), entity.getRegisteredBy().getRole()
        );
        user.setIdUser(entity.getRegisteredBy().getId());

        AnimalMovement movement = AnimalMovement.create(lot, entity.getEntryDate(), user);
        movement.setIdMovement(entity.getId());
        if (entity.getExitDate() != null) {
            movement.closeMovement(entity.getExitDate());
        }
        return movement;
    }
}