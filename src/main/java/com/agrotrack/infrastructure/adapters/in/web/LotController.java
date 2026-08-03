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
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<LotDto> createLot(@PathVariable UUID farmId, @RequestBody LotDto lotDto) {
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
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<LotDto> updateLot(@PathVariable UUID id, @RequestBody LotDto lotDto) {
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
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<LotDto>> getLotsByFarm(@PathVariable UUID farmId) {
        List<LotDto> lots = getLotsByFarmUseCase.executeGetLotsByFarm(farmId);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<LotDto>> getUnassignedLots(@org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        UUID userId = userDetails.getUser().getIdUser();
        List<LotDto> unassignedLots = getUnassignedLotsUseCase.executeGetUnassignedLots(userId)
                .stream()
                .map(mapper::toLotDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(unassignedLots);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> revokeLot(@PathVariable UUID id, @RequestParam String reason) {
        revokeLotUseCase.execute(id, reason);
        return ResponseEntity.noContent().build();
    }
}
