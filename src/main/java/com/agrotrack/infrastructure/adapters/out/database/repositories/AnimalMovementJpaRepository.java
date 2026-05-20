package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnimalMovementJpaRepository extends JpaRepository<AnimalMovementJpaEntity, UUID> {

    @Query(value = "SELECT * FROM animal_movements WHERE animal_id = :animalId AND exit_date IS NULL LIMIT 1", nativeQuery = true)
    Optional<AnimalMovementJpaEntity> findActiveMovementByAnimalId(@Param("animalId") UUID animalId);

    @Query(value = "SELECT * FROM animal_movements WHERE animal_id = :animalId", nativeQuery = true)
    List<AnimalMovementJpaEntity> findAllByAnimalId(@Param("animalId") UUID animalId);
}