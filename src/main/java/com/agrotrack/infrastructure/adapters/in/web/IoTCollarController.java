package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.CollarBatteryDto;
import com.agrotrack.application.dto.CollarStateDto;
import com.agrotrack.application.dto.GPSPositionDto;
import com.agrotrack.application.dto.IoTCollarDto;
import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.port.in.iot.RegisterGPSPositionUseCase;
import com.agrotrack.domain.port.in.iot.RegisterIoTCollarUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarBatteryUseCase;
import com.agrotrack.domain.port.in.iot.UpdateCollarStateUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collars")
public class IoTCollarController {

    private final RegisterIoTCollarUseCase registerIoTCollarUseCase;
    private final RegisterGPSPositionUseCase registerGPSPositionUseCase;
    private final UpdateCollarBatteryUseCase updateCollarBatteryUseCase;
    private final UpdateCollarStateUseCase updateCollarStateUseCase;

    public IoTCollarController(RegisterIoTCollarUseCase registerIoTCollarUseCase, RegisterGPSPositionUseCase registerGPSPositionUseCase,
                               UpdateCollarBatteryUseCase updateCollarBatteryUseCase, UpdateCollarStateUseCase updateCollarStateUseCase) {
        this.registerIoTCollarUseCase = registerIoTCollarUseCase;
        this.registerGPSPositionUseCase = registerGPSPositionUseCase;
        this.updateCollarBatteryUseCase = updateCollarBatteryUseCase;
        this.updateCollarStateUseCase = updateCollarStateUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<IoTCollar> createCollar(@RequestBody IoTCollarDto dto) {
        IoTCollar collar = registerIoTCollarUseCase.executeRegisterIoTCollar(
                dto.codeRFID(), dto.model(), dto.state(), dto.batteryLevel()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(collar);
    }

    @PostMapping("/{collarId}/gps")
    // Este endpoint podría no tener PreAuthorize de usuario si es llamado por un Webhook/IoT device,
    // o tener un Rol especial para dispositivos IoT. Por ahora dejamos OWNER/FOREMAN para consistencia.
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> registerGPS(@PathVariable UUID collarId, @RequestBody GPSPositionDto dto) {
        // Mapeo manual de lat/lon a Point JTS se debe hacer aquí, o se asume que el backend lo manejará
        // Para simplificar, usaremos un factoría de JTS Point en el controller o servicio.
        org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory();
        org.locationtech.jts.geom.Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(dto.longitude(), dto.latitude()));
        
        registerGPSPositionUseCase.executeRegisterGPSPosition(collarId, dto.timestamp(), point);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{collarId}/battery")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> updateBattery(@PathVariable UUID collarId, @RequestBody CollarBatteryDto dto) {
        updateCollarBatteryUseCase.executeUpdateCollarBattery(collarId, dto.batteryLevel());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{collarId}/state")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> updateState(@PathVariable UUID collarId, @RequestBody CollarStateDto dto) {
        updateCollarStateUseCase.executeUpdateCollarState(collarId, dto.state());
        return ResponseEntity.ok().build();
    }
}
