package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.HealthEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HealthEventJpaRepository extends JpaRepository<HealthEventJpaEntity, UUID> {
    // Usamos query nativa porque el animal_id se generó mediante el @JoinColumn de la entidad padre
    @Query(value = "SELECT * FROM health_events WHERE animal_id = :animalId", nativeQuery = true)
    List<HealthEventJpaEntity> findByAnimalId(@Param("animalId") UUID animalId);
}