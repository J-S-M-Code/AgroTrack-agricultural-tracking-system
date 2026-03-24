package com.agrotrack.domain.port.out.field;

import com.agrotrack.domain.model.entities.ObservationField;

import java.util.List;
import java.util.UUID;

public interface ObservationFieldRepositoryPort {
    ObservationField save(ObservationField observationField);
    List<ObservationField> findByFieldId(UUID fieldId);
}
