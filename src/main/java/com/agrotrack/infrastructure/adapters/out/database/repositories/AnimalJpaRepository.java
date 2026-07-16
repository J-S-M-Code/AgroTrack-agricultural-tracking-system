package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.AnimalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnimalJpaRepository extends JpaRepository<AnimalJpaEntity, UUID> {
    Optional<AnimalJpaEntity> findByCaravanSenasa(String caravanSenasa);
    boolean existsByCaravanSenasa(String caravanSenasa);
    Optional<AnimalJpaEntity> findByCollar_Id(UUID collarId);
    java.util.List<AnimalJpaEntity> findByAssignedLot_Farm_Id(UUID farmId);
}