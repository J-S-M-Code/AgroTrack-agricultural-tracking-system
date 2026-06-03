package com.agrotrack.infrastructure.adapters.out.persistence.repository;

import com.agrotrack.infrastructure.adapters.out.persistence.entity.FarmJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataFarmRepository extends JpaRepository<FarmJpaEntity, UUID> {
    
    @Query(value = "SELECT f.* FROM farm f INNER JOIN user_farm uf ON f.id_farm = uf.farm_id WHERE uf.user_id = :userId", nativeQuery = true)
    List<FarmJpaEntity> findAllByUserId(@Param("userId") UUID userId);
}
