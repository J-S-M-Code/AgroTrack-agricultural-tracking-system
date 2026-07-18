package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.Sex;
import com.agrotrack.domain.model.enums.Species;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnimalDto(
        UUID idAnimal,
        String visualCaravan,
        String caravanSenasa,
        String livestockKey,
        String numRENSPA,
        String internalManagementCaravan,
        Species species,
        String race,
        Sex sex,
        CategoryAnimal category,
        LocalDateTime birthdate,
        double currentWeight,
        UUID assignedLotId,
        UUID assignedCollarId
) {}
