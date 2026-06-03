package com.agrotrack.domain.port.out;

import com.agrotrack.domain.model.entities.Farm;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FarmRepositoryPort {
    Farm save(Farm farm);
    Optional<Farm> findById(UUID id);
    List<Farm> findAll();
    List<Farm> findByUserId(UUID userId); // Utilizado para obtener las fincas asignadas a un usuario
}
