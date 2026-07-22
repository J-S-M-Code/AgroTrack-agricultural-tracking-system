package com.agrotrack.domain.port.in.animal;

import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.Sex;
import com.agrotrack.domain.model.enums.Species;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateAnimalUseCase {
    Animal executeUpdateAnimal(UUID id, String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                               String internalManagementCaravan, Species species, String race, Sex sex,
                               CategoryAnimal category, LocalDateTime birthdate, Double currentWeight, UUID assignedLotId, UUID assignedCollarId);
}
