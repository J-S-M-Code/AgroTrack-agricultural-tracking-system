package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.CollarBatteryDto;
import com.agrotrack.application.dto.CollarStateDto;
import com.agrotrack.application.dto.GPSPositionDto;
import com.agrotrack.application.dto.IoTCollarDto;
import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.port.in.iot.RegisterGPSPositionUseCase;
import com.agrotrack.domain.port.in.iot.RegisterIoTCollarUseCase;
import com.agrotrack.domain.port.in.iot.GetCollarsByFarmUseCase;
import com.agrotrack.domain.port.in.iot.DeleteIoTCollarUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarBatteryUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarStateUseCase;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.enums.State;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@RestController
public class IoTCollarController {

    private final RegisterIoTCollarUseCase registerIoTCollarUseCase;
    private final RegisterGPSPositionUseCase registerGPSPositionUseCase;
    private final UpdateCollarBatteryUseCase updateCollarBatteryUseCase;
    private final UpdateCollarStateUseCase updateCollarStateUseCase;
    private final GetCollarsByFarmUseCase getCollarsByFarmUseCase;
    private final DeleteIoTCollarUseCase deleteIoTCollarUseCase;
    private final com.agrotrack.domain.port.in.iot.UpdateIoTCollarUseCase updateIoTCollarUseCase;
    private final AnimalRepositoryPort animalRepositoryPort;

    public IoTCollarController(RegisterIoTCollarUseCase registerIoTCollarUseCase, RegisterGPSPositionUseCase registerGPSPositionUseCase,
                               UpdateCollarBatteryUseCase updateCollarBatteryUseCase, UpdateCollarStateUseCase updateCollarStateUseCase,
                               GetCollarsByFarmUseCase getCollarsByFarmUseCase, DeleteIoTCollarUseCase deleteIoTCollarUseCase,
                               com.agrotrack.domain.port.in.iot.UpdateIoTCollarUseCase updateIoTCollarUseCase,
                               AnimalRepositoryPort animalRepositoryPort) {
        this.registerIoTCollarUseCase = registerIoTCollarUseCase;
        this.registerGPSPositionUseCase = registerGPSPositionUseCase;
        this.updateCollarBatteryUseCase = updateCollarBatteryUseCase;
        this.updateCollarStateUseCase = updateCollarStateUseCase;
        this.getCollarsByFarmUseCase = getCollarsByFarmUseCase;
        this.deleteIoTCollarUseCase = deleteIoTCollarUseCase;
        this.updateIoTCollarUseCase = updateIoTCollarUseCase;
        this.animalRepositoryPort = animalRepositoryPort;
    }

    @PostMapping("/api/v1/farms/{farmId}/collars")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<IoTCollar> createCollar(@PathVariable UUID farmId, @RequestBody IoTCollarDto dto) {
        IoTCollar collar = registerIoTCollarUseCase.executeRegisterIoTCollar(
                dto.codeRFID(), dto.model(), dto.state(), dto.batteryLevel(), farmId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(collar);
    }

    @GetMapping("/api/v1/farms/{farmId}/collars")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<List<IoTCollarResponse>> getCollarsByFarm(@PathVariable UUID farmId) {
        List<IoTCollar> collars = getCollarsByFarmUseCase.executeGetCollarsByFarm(farmId);
        List<IoTCollarResponse> dtos = collars.stream().map(collar -> {
             Optional<Animal> animal = animalRepositoryPort.findByCollarId(collar.getIdCollar());
             String assignedCaravan = animal.map(Animal::getInternalManagementCaravan).orElse(null);
             return new IoTCollarResponse(collar, assignedCaravan);
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    public record IoTCollarResponse(
            UUID idCollar,
            String codeRFID,
            String model,
            State state,
            Double batteryLevel,
            String assignedAnimalCaravan
    ) {
        public IoTCollarResponse(IoTCollar c, String caravan) {
            this(c.getIdCollar(), c.getCodeRFID(), c.getModel(), c.getState(), c.getBatteryLevel(), caravan);
        }
    }

    @DeleteMapping("/api/v1/collars/{collarId}")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> deleteCollar(@PathVariable UUID collarId, @RequestBody DeleteReasonDto reasonDto, @RequestParam UUID farmId) {
        deleteIoTCollarUseCase.executeDeleteIoTCollar(collarId, reasonDto.reason());
        return ResponseEntity.noContent().build();
    }

    public record DeleteReasonDto(String reason) {}

    @PutMapping("/api/v1/collars/{collarId}")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<IoTCollar> updateCollar(@PathVariable UUID collarId, @RequestBody IoTCollarDto dto, @RequestParam UUID farmId) {
        IoTCollar updatedCollar = updateIoTCollarUseCase.executeUpdateIoTCollar(
                collarId, dto.codeRFID(), dto.model(), dto.state()
        );
        return ResponseEntity.ok(updatedCollar);
    }

    @PostMapping("/api/v1/collars/{collarId}/gps")
    // Este endpoint podría no tener PreAuthorize de usuario si es llamado por un Webhook/IoT device,
    // o tener un Rol especial para dispositivos IoT. Por ahora dejamos FOREMAN para consistencia.
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> registerGPS(@PathVariable UUID collarId, @RequestBody GPSPositionDto dto, @RequestParam UUID farmId) {
        // Mapeo manual de lat/lon a Point JTS se debe hacer aquí, o se asume que el backend lo manejará
        // Para simplificar, usaremos un factoría de JTS Point en el controller o servicio.
        org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory();
        org.locationtech.jts.geom.Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(dto.longitude(), dto.latitude()));
        
        registerGPSPositionUseCase.executeRegisterGPSPosition(collarId, dto.timestamp(), point);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/api/v1/collars/{collarId}/battery")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> updateBattery(@PathVariable UUID collarId, @RequestBody CollarBatteryDto dto, @RequestParam UUID farmId) {
        updateCollarBatteryUseCase.executeUpdateCollarBattery(collarId, dto.batteryLevel());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/v1/collars/{collarId}/state")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> updateState(@PathVariable UUID collarId, @RequestBody CollarStateDto dto, @RequestParam UUID farmId) {
        updateCollarStateUseCase.executeUpdateCollarState(collarId, dto.state());
        return ResponseEntity.ok().build();
    }
}
