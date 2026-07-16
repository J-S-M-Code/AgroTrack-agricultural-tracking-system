package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<UserJpaEntity> findByManagedFarms_Id(UUID farmId);
}