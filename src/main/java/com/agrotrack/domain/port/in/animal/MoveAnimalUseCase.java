package com.agrotrack.domain.port.in.animal;

import java.time.LocalDateTime;
import java.util.UUID;

public interface MoveAnimalUseCase {
    // Este caso de uso se encargará de buscar el movimiento activo, cerrarlo,
    // crear uno nuevo hacia el originLotId y actualizar el assignedLot en el Animal.
    void execute(UUID animalId, UUID destinationLotId, LocalDateTime entryDate, UUID registeredByUserId);
}