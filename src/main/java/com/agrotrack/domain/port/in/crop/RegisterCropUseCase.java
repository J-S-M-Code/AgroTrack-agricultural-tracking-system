package com.agrotrack.domain.port.in.crop;

import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RegisterCropUseCase {
    Crop executeRegisterCrop(TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                 LocalDateTime estimateHarvestDate, UUID assignedLotId, double implantedSurface,
                 String renspa, PhenologicalState phenologicalState);
}