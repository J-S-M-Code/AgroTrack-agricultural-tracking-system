package com.agrotrack.domain.port.out.field;


import com.agrotrack.domain.model.entities.Field;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldRepositoryPort {
    Field save(Field field);
    Optional<Field> findById(UUID id);
    List<Field> findByLandId(UUID id);
    boolean existsOverLappingField(Polygon newPerimeter, UUID excludeFieldId);
}
