package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.CropDto;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.port.in.crop.RegisterCropUseCase;
import com.agrotrack.domain.port.in.crop.RegisterHarvestUseCase;
import com.agrotrack.domain.port.in.crop.UpdatePhenologicalStateUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CropController {

    private final RegisterCropUseCase registerCropUseCase;
    private final UpdatePhenologicalStateUseCase updatePhenologicalStateUseCase;
    private final RegisterHarvestUseCase registerHarvestUseCase;

    public CropController(RegisterCropUseCase registerCropUseCase, UpdatePhenologicalStateUseCase updatePhenologicalStateUseCase, RegisterHarvestUseCase registerHarvestUseCase) {
        this.registerCropUseCase = registerCropUseCase;
        this.updatePhenologicalStateUseCase = updatePhenologicalStateUseCase;
        this.registerHarvestUseCase = registerHarvestUseCase;
    }

    @PostMapping("/lots/{lotId}/crops")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<Crop> createCrop(@PathVariable UUID lotId, @RequestBody CropDto dto) {
        Crop createdCrop = registerCropUseCase.executeRegisterCrop(
                dto.typeCrop(), dto.species(), dto.variety(), dto.plantingDate(),
                dto.estimateHarvestDate(), lotId, dto.implantedSurface(),
                dto.renspa(), dto.phenologicalState()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCrop);
    }

    @PutMapping("/crops/{cropId}/phenological-state")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<Void> updatePhenologicalState(@PathVariable UUID cropId, @RequestParam PhenologicalState state) {
        updatePhenologicalStateUseCase.executeUpdatePhenologicalState(cropId, state);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/crops/{cropId}/harvest")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST')")
    public ResponseEntity<Void> registerHarvest(@PathVariable UUID cropId, @RequestParam LocalDateTime actualHarvestDate) {
        registerHarvestUseCase.executeRegisterHarvest(cropId, actualHarvestDate);
        return ResponseEntity.ok().build();
    }
}
