package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.RecordType;
import com.agrotrack.domain.port.in.alert.CreateAlertUseCase;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase;
import com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService implements CreateAlertUseCase, GetAlertsByFarmUseCase, GetAlertByIdUseCase {

    private final AlertRepositoryPort alertRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final CropRepositoryPort cropRepositoryPort;
    private final AnimalRepositoryPort animalRepositoryPort;

    public AlertService(AlertRepositoryPort alertRepositoryPort, UserRepositoryPort userRepositoryPort,
                        LotRepositoryPort lotRepositoryPort, CropRepositoryPort cropRepositoryPort,
                        AnimalRepositoryPort animalRepositoryPort) {
        this.alertRepositoryPort = alertRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.cropRepositoryPort = cropRepositoryPort;
        this.animalRepositoryPort = animalRepositoryPort;
    }

    @Override
    @Transactional
    public Alert execute(String title, AlertType alertType, Priority priority, RecordType recordType,
                         String description, LocalDateTime createdAt, UUID authorId, List<String> images,
                         UUID relatedLotId, UUID relatedCropId, UUID relatedAnimalId,
                         Polygon polygonLimit, Point centroid) {

        // 1. Buscar al autor
        User author = userRepositoryPort.findById(authorId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Autor de la alerta no encontrado"));

        // 2. Buscar relaciones opcionales (por lo menos una debe existir, el dominio lo valida en el create)
        Lot lot = (relatedLotId != null) ? lotRepositoryPort.findById(relatedLotId).orElse(null) : null;
        Crop crop = (relatedCropId != null) ? cropRepositoryPort.findById(relatedCropId).orElse(null) : null;
        Animal animal = (relatedAnimalId != null) ? animalRepositoryPort.findById(relatedAnimalId).orElse(null) : null;

        // 3. Crear la alerta
        Alert newAlert = Alert.create(
                title, alertType, priority, recordType, description, createdAt,
                author, images, lot, crop, animal, polygonLimit, centroid
        );

        // 4. Guardar
        return alertRepositoryPort.save(newAlert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> executeGetAlertsByFarm(UUID farmId) {
        return alertRepositoryPort.findByFarmId(farmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Alert executeGetAlertById(UUID alertId) {
        return alertRepositoryPort.findById(alertId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Alerta no encontrada con ID: " + alertId));
    }
}