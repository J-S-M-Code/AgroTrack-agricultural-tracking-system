package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.LotDto;
// import com.agrotrack.domain.port.in.lot.CreateLotUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lots")
public class LotController {

    // private final CreateLotUseCase createLotUseCase;
    // inyectar dependencias...

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<LotDto> createLot(@RequestBody LotDto lotDto) {
        // Solo OWNER y FOREMAN pueden crear/modificar la geometría de lotes.
        // AGRONOMIST puede verlos y asociar cultivos.
        return ResponseEntity.ok(lotDto); // Simulación
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<LotDto>> getLotsByFarm(@PathVariable UUID farmId) {
        // Todos pueden ver los lotes de la finca a la que pertenecen.
        return ResponseEntity.ok(List.of()); 
    }
}
