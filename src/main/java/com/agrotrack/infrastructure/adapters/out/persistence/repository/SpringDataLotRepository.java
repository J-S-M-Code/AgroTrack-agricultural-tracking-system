package com.agrotrack.infrastructure.adapters.out.persistence.repository;

import com.agrotrack.infrastructure.adapters.out.persistence.entity.LotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLotRepository extends JpaRepository<LotJpaEntity, UUID> {
    
    List<LotJpaEntity> findByFarmIdFarm(UUID farmId);
}
