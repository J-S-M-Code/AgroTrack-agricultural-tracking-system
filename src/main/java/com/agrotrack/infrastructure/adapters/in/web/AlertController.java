package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.AlertDto;
import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.port.in.alert.CreateAlertUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final CreateAlertUseCase createAlertUseCase;
    private final com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase getAlertsByFarmUseCase;
    private final com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase getAlertByIdUseCase;

    public AlertController(CreateAlertUseCase createAlertUseCase,
                           com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase getAlertsByFarmUseCase,
                           com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase getAlertByIdUseCase) {
        this.createAlertUseCase = createAlertUseCase;
        this.getAlertsByFarmUseCase = getAlertsByFarmUseCase;
        this.getAlertByIdUseCase = getAlertByIdUseCase;
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR')")
    public ResponseEntity<java.util.List<Alert>> getAlertsByFarm(@PathVariable java.util.UUID farmId) {
        return ResponseEntity.ok(getAlertsByFarmUseCase.executeGetAlertsByFarm(farmId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR')")
    public ResponseEntity<Alert> getAlertById(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(getAlertByIdUseCase.executeGetAlertById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR')")
    public ResponseEntity<Alert> createAlert(@RequestBody AlertDto dto) {
        Alert createdAlert = createAlertUseCase.execute(
                dto.title(), dto.alertType(), dto.priority(), dto.recordType(),
                dto.description(), dto.createdAt(), dto.authorId(), dto.images(),
                dto.relatedLotId(), dto.relatedCropId(), dto.relatedAnimalId(),
                dto.polygonLimit(), dto.centroid()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlert);
    }
}
