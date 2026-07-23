package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.AnimalDto;
import com.agrotrack.application.dto.HealthEventDto;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.port.in.animal.MoveAnimalUseCase;
import com.agrotrack.domain.port.in.animal.RegisterAnimalUseCase;
import com.agrotrack.domain.port.in.animal.RegisterHealthEventUseCase;
import com.agrotrack.domain.port.in.animal.GetAnimalByIdUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/animals")
public class AnimalController {

    private final RegisterAnimalUseCase registerAnimalUseCase;
    private final MoveAnimalUseCase moveAnimalUseCase;
    private final RegisterHealthEventUseCase registerHealthEventUseCase;
    private final GetAnimalByIdUseCase getAnimalByIdUseCase;
    private final com.agrotrack.domain.port.in.animal.GetAnimalsByFarmUseCase getAnimalsByFarmUseCase;
    private final com.agrotrack.domain.port.in.animal.GetUnassignedAnimalsUseCase getUnassignedAnimalsUseCase;
    private final com.agrotrack.domain.port.in.animal.UpdateAnimalUseCase updateAnimalUseCase;
    private final com.agrotrack.domain.port.in.animal.DeleteAnimalUseCase deleteAnimalUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public AnimalController(RegisterAnimalUseCase registerAnimalUseCase, MoveAnimalUseCase moveAnimalUseCase,
                            RegisterHealthEventUseCase registerHealthEventUseCase, GetAnimalByIdUseCase getAnimalByIdUseCase,
                            com.agrotrack.domain.port.in.animal.GetAnimalsByFarmUseCase getAnimalsByFarmUseCase,
                            com.agrotrack.domain.port.in.animal.GetUnassignedAnimalsUseCase getUnassignedAnimalsUseCase,
                            com.agrotrack.domain.port.in.animal.UpdateAnimalUseCase updateAnimalUseCase,
                            com.agrotrack.domain.port.in.animal.DeleteAnimalUseCase deleteAnimalUseCase,
                            com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.registerAnimalUseCase = registerAnimalUseCase;
        this.moveAnimalUseCase = moveAnimalUseCase;
        this.registerHealthEventUseCase = registerHealthEventUseCase;
        this.getAnimalByIdUseCase = getAnimalByIdUseCase;
        this.getAnimalsByFarmUseCase = getAnimalsByFarmUseCase;
        this.getUnassignedAnimalsUseCase = getUnassignedAnimalsUseCase;
        this.updateAnimalUseCase = updateAnimalUseCase;
        this.deleteAnimalUseCase = deleteAnimalUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<java.util.List<AnimalDto>> getAnimalsByFarm(@PathVariable UUID farmId) {
        return ResponseEntity.ok(mapper.toAnimalDtoList(getAnimalsByFarmUseCase.executeGetAnimalsByFarm(farmId)));
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<java.util.List<AnimalDto>> getUnassignedAnimals(@org.springframework.security.core.annotation.AuthenticationPrincipal com.agrotrack.infrastructure.security.CustomUserDetails userDetails) {
        return ResponseEntity.ok(mapper.toAnimalDtoList(getUnassignedAnimalsUseCase.executeGetUnassignedAnimals(userDetails.getUser().getIdUser())));
    }

    @GetMapping("/test/{farmId}")
    public ResponseEntity<java.util.List<AnimalDto>> testGetAnimalsByFarm(@PathVariable UUID farmId) {
        System.out.println("Calling test endpoint for farmId: " + farmId);
        java.util.List<Animal> animals = getAnimalsByFarmUseCase.executeGetAnimalsByFarm(farmId);
        System.out.println("Found animals: " + animals.size());
        return ResponseEntity.ok(mapper.toAnimalDtoList(animals));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<AnimalDto> createAnimal(@RequestBody AnimalDto dto) {
        Animal createdAnimal = registerAnimalUseCase.executeRegisterAnimal(
                dto.visualCaravan(), dto.caravanSenasa(), dto.livestockKey(), dto.numRENSPA(),
                dto.internalManagementCaravan(), dto.species(), dto.race(), dto.sex(),
                dto.category(), dto.birthdate(), dto.currentWeight(), dto.assignedLotId(), dto.assignedCollarId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAnimalDto(createdAnimal));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<AnimalDto> updateAnimal(@PathVariable UUID id, @RequestBody AnimalDto dto) {
        Animal updatedAnimal = updateAnimalUseCase.executeUpdateAnimal(
                id, dto.visualCaravan(), dto.caravanSenasa(), dto.livestockKey(), dto.numRENSPA(),
                dto.internalManagementCaravan(), dto.species(), dto.race(), dto.sex(),
                dto.category(), dto.birthdate(), dto.currentWeight(), dto.assignedLotId(), dto.assignedCollarId()
        );
        return ResponseEntity.ok(mapper.toAnimalDto(updatedAnimal));
    }

    @PutMapping("/{id}/move")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> moveAnimal(@PathVariable UUID id, @RequestParam UUID destinationLotId, @RequestParam UUID registeredByUserId) {
        moveAnimalUseCase.executeMoveAnimal(id, destinationLotId, LocalDateTime.now(), registeredByUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/health-events")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN', 'VETERINARIAN')")
    public ResponseEntity<Void> registerHealthEvent(@PathVariable UUID id, @RequestBody HealthEventDto dto) {
        registerHealthEventUseCase.executeRegisterHealthEvent(
                id, dto.date(), dto.type(), dto.treatment(), dto.numAct(), dto.observation(), dto.veterinarianId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN', 'VETERINARIAN')")
    public ResponseEntity<AnimalDto> getAnimal(@PathVariable UUID id) {
        return getAnimalByIdUseCase.executeGetAnimalById(id)
                .map(mapper::toAnimalDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> deleteAnimal(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        deleteAnimalUseCase.executeDeleteAnimal(id, reason);
        return ResponseEntity.ok().build();
    }
}
