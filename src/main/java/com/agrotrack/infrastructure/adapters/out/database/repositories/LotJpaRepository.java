package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LotJpaRepository extends JpaRepository<LotJpaEntity, UUID> {

    List<LotJpaEntity> findByFarmId(UUID farmId);

    @Query(value = "SELECT l.* FROM lots l " +
            "JOIN farms f ON l.farm_id = f.id " +
            "JOIN user_farm_access uf ON f.id = uf.farm_id " +
            "WHERE uf.user_id = :userId AND f.is_active = false", nativeQuery = true)
    List<LotJpaEntity> findUnassignedLotsForUser(@Param("userId") UUID userId);

    // Validación PostGIS para evitar que dos lotes se pisen entre sí
    @Query("SELECT COUNT(l) > 0 FROM LotJpaEntity l WHERE " +
            "ST_Intersects(l.polygonLimit, :newPerimeter) = true " +
            "AND (:excludeLotId IS NULL OR l.id != :excludeLotId) " +
            "AND l.type IN (com.agrotrack.domain.model.enums.LotType.PASTURE, com.agrotrack.domain.model.enums.LotType.AGRICULTURAL)")
    boolean existsOverlappingLot(@Param("newPerimeter") Polygon newPerimeter,
                                 @Param("excludeLotId") UUID excludeLotId);
}