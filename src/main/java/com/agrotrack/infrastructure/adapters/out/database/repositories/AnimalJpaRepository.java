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

    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT a.* FROM animals a " +
            "JOIN lots l ON a.assigned_lot_id = l.id " +
            "JOIN farms f ON l.farm_id = f.id " +
            "JOIN user_farm_access uf ON f.id = uf.farm_id " +
            "WHERE uf.user_id = :userId AND a.is_active = true AND (f.is_active = false OR l.is_active = false)", nativeQuery = true)
    java.util.List<AnimalJpaEntity> findUnassignedAnimalsForUser(@org.springframework.data.repository.query.Param("userId") UUID userId);
}