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
import com.agrotrack.domain.port.in.crop.UpdateCropUseCase;
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
    private final com.agrotrack.domain.port.in.crop.GetCropsByFarmUseCase getCropsByFarmUseCase;
    private final com.agrotrack.domain.port.in.crop.GetUnassignedCropsUseCase getUnassignedCropsUseCase;
    private final com.agrotrack.domain.port.in.crop.GetCropByIdUseCase getCropByIdUseCase;
    private final UpdateCropUseCase updateCropUseCase;
    private final com.agrotrack.domain.port.in.crop.DeleteCropUseCase deleteCropUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public CropController(RegisterCropUseCase registerCropUseCase, UpdatePhenologicalStateUseCase updatePhenologicalStateUseCase, 
                          RegisterHarvestUseCase registerHarvestUseCase, 
                          com.agrotrack.domain.port.in.crop.GetCropsByFarmUseCase getCropsByFarmUseCase,
                          com.agrotrack.domain.port.in.crop.GetUnassignedCropsUseCase getUnassignedCropsUseCase,
                          com.agrotrack.domain.port.in.crop.GetCropByIdUseCase getCropByIdUseCase,
                          UpdateCropUseCase updateCropUseCase,
                          com.agrotrack.domain.port.in.crop.DeleteCropUseCase deleteCropUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.registerCropUseCase = registerCropUseCase;
        this.updatePhenologicalStateUseCase = updatePhenologicalStateUseCase;
        this.registerHarvestUseCase = registerHarvestUseCase;
        this.getCropsByFarmUseCase = getCropsByFarmUseCase;
        this.getUnassignedCropsUseCase = getUnassignedCropsUseCase;
        this.getCropByIdUseCase = getCropByIdUseCase;
        this.updateCropUseCase = updateCropUseCase;
        this.deleteCropUseCase = deleteCropUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/crops/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'WORKER')")
    public ResponseEntity<java.util.List<CropDto>> getCropsByFarm(@PathVariable UUID farmId) {
        return ResponseEntity.ok(mapper.toCropDtoList(getCropsByFarmUseCase.executeGetCropsByFarm(farmId)));
    }

    @GetMapping("/crops/unassigned")
    @PreAuthorize("hasPermission('ANY_FARM', 'WORKER')")
    public ResponseEntity<java.util.List<CropDto>> getUnassignedCrops(@org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        return ResponseEntity.ok(mapper.toCropDtoList(getUnassignedCropsUseCase.executeGetUnassignedCrops(userDetails.getUser().getIdUser())));
    }

    @GetMapping("/crops/{id}")
    @PreAuthorize("hasPermission(#farmId, 'WORKER')")
    public ResponseEntity<CropDto> getCropById(@PathVariable UUID id, @RequestParam UUID farmId) {
        return ResponseEntity.ok(mapper.toCropDto(getCropByIdUseCase.executeGetCropById(id)));
    }

    @PostMapping("/lots/{lotId}/crops")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<CropDto> createCrop(@PathVariable UUID lotId, @RequestBody CropDto dto, @RequestParam UUID farmId) {
        Crop createdCrop = registerCropUseCase.executeRegisterCrop(
                dto.typeCrop(), dto.species(), dto.variety(), dto.plantingDate(),
                dto.estimateHarvestDate(), lotId, dto.implantedSurface(),
                dto.renspa(), dto.phenologicalState()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCropDto(createdCrop));
    }

    @PutMapping("/crops/{cropId}")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<CropDto> updateCrop(@PathVariable UUID cropId, @RequestBody CropDto dto, @RequestParam UUID farmId) {
        Crop updatedCrop = updateCropUseCase.executeUpdateCrop(
                cropId, dto.typeCrop(), dto.species(), dto.variety(), dto.plantingDate(),
                dto.estimateHarvestDate(), dto.lotId(), dto.implantedSurface(),
                dto.renspa(), dto.phenologicalState()
        );
        return ResponseEntity.ok(mapper.toCropDto(updatedCrop));
    }

    @PutMapping("/crops/{cropId}/phenological-state")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<Void> updatePhenologicalState(@PathVariable UUID cropId, @RequestParam PhenologicalState state, @RequestParam UUID farmId) {
        updatePhenologicalStateUseCase.executeUpdatePhenologicalState(cropId, state);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/crops/{cropId}/harvest")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<Void> registerHarvest(@PathVariable UUID cropId, @RequestParam LocalDateTime actualHarvestDate, @RequestParam UUID farmId) {
        registerHarvestUseCase.executeRegisterHarvest(cropId, actualHarvestDate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/crops/{cropId}")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST')")
    public ResponseEntity<Void> deleteCrop(@PathVariable UUID cropId, @RequestParam(required = false) String reason, @RequestParam UUID farmId) {
        deleteCropUseCase.executeDeleteCrop(cropId, reason);
        return ResponseEntity.ok().build();
    }
}
