package com.agrotrack.domain.port.out.animal;

import com.agrotrack.domain.model.entities.HistoryAnimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistoryAnimalRepositoryPort {
    HistoryAnimal save(HistoryAnimal historyAnimal);
    Optional<HistoryAnimal> findLastPositionByAnimalId(UUID id);
    List<HistoryAnimal> findPositionByAnimalIdAndDateRange(UUID id, LocalDateTime dateStart, LocalDateTime dateEnd);
}
