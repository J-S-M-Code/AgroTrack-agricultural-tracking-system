package com.agrotrack.infrastructure.adapters.out.persistence.repository;

import com.agrotrack.infrastructure.adapters.out.persistence.entity.UserInviteTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInviteTokenJpaRepository extends JpaRepository<UserInviteTokenJpaEntity, UUID> {
    Optional<UserInviteTokenJpaEntity> findByToken(String token);
}
