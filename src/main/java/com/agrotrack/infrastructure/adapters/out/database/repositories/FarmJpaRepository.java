package com.agrotrack.infrastructure.adapters.out.database.repositories;

import com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FarmJpaRepository extends JpaRepository<FarmJpaEntity, UUID> {

    boolean existsByCuit(String cuit);

    // Consulta espacial nativa de PostGIS a través de Hibernate Spatial
    // Verifica si el polígono nuevo se cruza con alguno existente en la base de datos
    @Query("SELECT COUNT(f) > 0 FROM FarmJpaEntity f WHERE " +
            "ST_Intersects(f.polygonLimit, :newPerimeter) = true " +
            "AND (:excludeFarmId IS NULL OR f.id != :excludeFarmId)")
    boolean existsOverlappingFarm(@Param("newPerimeter") Polygon newPerimeter,
                                  @Param("excludeFarmId") UUID excludeFarmId);
}