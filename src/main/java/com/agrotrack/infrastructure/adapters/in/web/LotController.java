package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.LotDto;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.in.lot.CreateLotUseCase;
import com.agrotrack.domain.port.in.lot.GetLotsByFarmUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lots")
public class LotController {

    private final CreateLotUseCase createLotUseCase;
    private final GetLotsByFarmUseCase getLotsByFarmUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public LotController(CreateLotUseCase createLotUseCase, GetLotsByFarmUseCase getLotsByFarmUseCase,
                         com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.createLotUseCase = createLotUseCase;
        this.getLotsByFarmUseCase = getLotsByFarmUseCase;
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

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<LotDto>> getLotsByFarm(@PathVariable UUID farmId) {
        List<LotDto> lots = getLotsByFarmUseCase.executeGetLotsByFarm(farmId);
        return ResponseEntity.ok(lots);
    }
}
