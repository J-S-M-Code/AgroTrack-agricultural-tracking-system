package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.LotDto;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.in.lot.CreateLotUseCase;
import com.agrotrack.domain.port.in.lot.GetLotsByFarmUseCase;
import com.agrotrack.domain.port.in.lot.GetUnassignedLotsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lots")
public class LotController {

    private final CreateLotUseCase createLotUseCase;
    private final GetLotsByFarmUseCase getLotsByFarmUseCase;
    private final GetUnassignedLotsUseCase getUnassignedLotsUseCase;
    private final com.agrotrack.domain.port.in.lot.RevokeLotUseCase revokeLotUseCase;
    private final com.agrotrack.domain.port.in.lot.UpdateLotUseCase updateLotUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public LotController(CreateLotUseCase createLotUseCase, GetLotsByFarmUseCase getLotsByFarmUseCase,
                         GetUnassignedLotsUseCase getUnassignedLotsUseCase,
                         com.agrotrack.domain.port.in.lot.RevokeLotUseCase revokeLotUseCase,
                         com.agrotrack.domain.port.in.lot.UpdateLotUseCase updateLotUseCase,
                         com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.createLotUseCase = createLotUseCase;
        this.getLotsByFarmUseCase = getLotsByFarmUseCase;
        this.getUnassignedLotsUseCase = getUnassignedLotsUseCase;
        this.revokeLotUseCase = revokeLotUseCase;
        this.updateLotUseCase = updateLotUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'AGRONOMIST') or hasPermission(#farmId, 'VETERINARIAN')")
    public ResponseEntity<?> createLot(@PathVariable UUID farmId, @RequestBody LotDto lotDto, @org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        java.util.Optional<com.agrotrack.domain.model.enums.UserRole> roleOpt = userDetails.getUser().getRoleForFarm(farmId);
        if (roleOpt.isPresent()) {
            com.agrotrack.domain.model.enums.UserRole role = roleOpt.get();
            if (role == com.agrotrack.domain.model.enums.UserRole.AGRONOMIST) {
                if (lotDto.getType() != com.agrotrack.domain.model.enums.LotType.AGRICULTURAL && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.RESERVE) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("Agronomists can only create AGRICULTURAL or RESERVE lots.");
                }
            } else if (role == com.agrotrack.domain.model.enums.UserRole.VETERINARIAN) {
                if (lotDto.getType() != com.agrotrack.domain.model.enums.LotType.PASTURE && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.WATER_ZONE && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.HANDLING_PEN && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.FEEDING_ZONE) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("Veterinarians can only create animal-related lots.");
                }
            }
        }

        Lot createdLot = createLotUseCase.executeCreateLot(
                farmId,
                lotDto.getName(),
                lotDto.getHectares(),
                lotDto.getSoilType(),
                lotDto.getType(),
                lotDto.getDescription(),
                lotDto.getPolygonLimit() != null ? lotDto.getPolygonLimit().toJtsPolygon() : null
        );

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/lots/{id}")
                .buildAndExpand(createdLot.getIdLot())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toLotDto(createdLot));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#lotDto.farmId, 'AGRONOMIST') or hasPermission(#lotDto.farmId, 'VETERINARIAN')")
    public ResponseEntity<?> updateLot(@PathVariable UUID id, @RequestBody LotDto lotDto, @org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        java.util.Optional<com.agrotrack.domain.model.enums.UserRole> roleOpt = userDetails.getUser().getRoleForFarm(lotDto.getFarmId());
        if (roleOpt.isPresent()) {
            com.agrotrack.domain.model.enums.UserRole role = roleOpt.get();
            if (role == com.agrotrack.domain.model.enums.UserRole.AGRONOMIST) {
                if (lotDto.getType() != com.agrotrack.domain.model.enums.LotType.AGRICULTURAL && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.RESERVE) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("Agronomists can only update AGRICULTURAL or RESERVE lots.");
                }
            } else if (role == com.agrotrack.domain.model.enums.UserRole.VETERINARIAN) {
                if (lotDto.getType() != com.agrotrack.domain.model.enums.LotType.PASTURE && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.WATER_ZONE && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.HANDLING_PEN && lotDto.getType() != com.agrotrack.domain.model.enums.LotType.FEEDING_ZONE) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body("Veterinarians can only update animal-related lots.");
                }
            }
        }

        Lot updatedLot = updateLotUseCase.executeUpdateLot(
                id,
                lotDto.getName(),
                lotDto.getHectares(),
                lotDto.getSoilType(),
                lotDto.getType(),
                lotDto.getDescription(),
                lotDto.getPolygonLimit() != null ? lotDto.getPolygonLimit().toJtsPolygon() : null
        );
        return ResponseEntity.ok(mapper.toLotDto(updatedLot));
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'ANY')")
    public ResponseEntity<List<LotDto>> getLotsByFarm(@PathVariable UUID farmId) {
        List<LotDto> lots = getLotsByFarmUseCase.executeGetLotsByFarm(farmId);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasPermission('ANY_FARM', 'ANY')") // Not well supported, but keeping it
    public ResponseEntity<List<LotDto>> getUnassignedLots(@org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        UUID userId = userDetails.getUser().getIdUser();
        List<LotDto> unassignedLots = getUnassignedLotsUseCase.executeGetUnassignedLots(userId)
                .stream()
                .map(mapper::toLotDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(unassignedLots);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#farmId, 'OWNER')")
    public ResponseEntity<Void> revokeLot(@PathVariable UUID id, @RequestParam String reason, @RequestParam UUID farmId) {
        // We added farmId to the signature for security validation
        revokeLotUseCase.execute(id, reason);
        return ResponseEntity.noContent().build();
    }
}
