package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.AlertDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.port.in.alert.CreateAlertUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final CreateAlertUseCase createAlertUseCase;
    private final com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase getAlertsByFarmUseCase;
    private final com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase getAlertByIdUseCase;
    private final com.agrotrack.domain.port.in.alert.DeleteAlertUseCase deleteAlertUseCase;
    private final ApplicationDtoMapper mapper;

    public AlertController(CreateAlertUseCase createAlertUseCase,
                           com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase getAlertsByFarmUseCase,
                           com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase getAlertByIdUseCase,
                           com.agrotrack.domain.port.in.alert.DeleteAlertUseCase deleteAlertUseCase,
                           ApplicationDtoMapper mapper) {
        this.createAlertUseCase = createAlertUseCase;
        this.getAlertsByFarmUseCase = getAlertsByFarmUseCase;
        this.getAlertByIdUseCase = getAlertByIdUseCase;
        this.deleteAlertUseCase = deleteAlertUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'APPLICATOR')")
    public ResponseEntity<java.util.List<AlertDto>> getAlertsByFarm(@PathVariable java.util.UUID farmId) {
        return ResponseEntity.ok(mapper.toAlertDtoList(getAlertsByFarmUseCase.executeGetAlertsByFarm(farmId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#farmId, 'APPLICATOR')")
    public ResponseEntity<AlertDto> getAlertById(@PathVariable java.util.UUID id, @RequestParam UUID farmId) {
        return ResponseEntity.ok(mapper.toAlertDto(getAlertByIdUseCase.executeGetAlertById(id)));
    }

    @PostMapping
    @PreAuthorize("hasPermission(#farmId, 'APPLICATOR')")
    public ResponseEntity<AlertDto> createAlert(@RequestBody AlertDto dto, @RequestParam UUID farmId) {
        Alert createdAlert = createAlertUseCase.execute(
                dto.title(), dto.alertType(), dto.priority(), dto.recordType(),
                dto.description(), dto.createdAt(), dto.authorId(), dto.images(),
                dto.relatedLotId(), dto.relatedCropId(), dto.relatedAnimalId(),
                dto.polygonLimit() != null ? dto.polygonLimit().toJtsPolygon() : null, 
                dto.centroid() != null ? dto.centroid().toJtsPoint() : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAlertDto(createdAlert));
    }

    @DeleteMapping("/{alertId}")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> deleteAlert(@PathVariable UUID alertId, @RequestParam UUID farmId) {
        deleteAlertUseCase.executeDeleteAlert(alertId);
        return ResponseEntity.ok().build();
    }
}
