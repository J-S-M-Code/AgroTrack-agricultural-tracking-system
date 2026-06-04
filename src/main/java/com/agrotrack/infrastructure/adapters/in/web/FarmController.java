package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.FarmDto;
// Importaremos los puertos de Farm (ej. CreateFarmUseCase, GetFarmUseCase) cuando los creemos.
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farms")
public class FarmController {

    // private final CreateFarmUseCase createFarmUseCase;
    // ... inyectar casos de uso

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<FarmDto> createFarm(@RequestBody FarmDto farmDto) {
        // Solo el OWNER puede registrar nuevas fincas
        // Llamada al caso de uso aquí
        return ResponseEntity.ok(farmDto); // Simulación
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<FarmDto>> getMyFarms() {
        // Todos pueden ver sus fincas asignadas, pero la implementación del UseCase filtrará 
        // qué fincas retorna basándose en el ID del usuario autenticado.
        return ResponseEntity.ok(List.of()); 
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN')")
    public ResponseEntity<FarmDto> getFarmDetails(@PathVariable UUID id) {
        // Solo ciertos roles con acceso gerencial/técnico ven el detalle profundo de la finca
        return ResponseEntity.ok(FarmDto.builder().idFarm(id).build());
    }
}
