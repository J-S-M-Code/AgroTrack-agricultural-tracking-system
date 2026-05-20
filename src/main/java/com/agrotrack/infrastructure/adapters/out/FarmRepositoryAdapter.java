package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // ¡Esta anotación soluciona el error de Autowire!
public class FarmRepositoryAdapter implements FarmRepositoryPort {

    // Aquí luego inyectarás tu FarmJpaRepository de Spring Data

    @Override
    public Farm save(Farm farm) {
        // Lógica para mapear la entidad de dominio a entidad JPA y guardar
        return farm;
    }

    @Override
    public Optional<Farm> findById(UUID id) {
        return Optional.empty(); // Lógica de búsqueda
    }

    @Override
    public boolean existsOverlappingFarm(Polygon newPerimeter, UUID excludeFarmId) {
        return false; // Lógica de PostGIS (ST_Intersects)
    }

    @Override
    public boolean existsByCuit(String cuit) {
        return false; // Lógica de búsqueda
    }
}