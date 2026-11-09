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
import com.agrotrack.domain.port.in.alert.DeleteAlertUseCase;
import com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase;
import com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase;
import com.agrotrack.domain.port.in.alert.UploadAlertImageUseCase;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AlertService implements CreateAlertUseCase, GetAlertsByFarmUseCase, GetAlertByIdUseCase, DeleteAlertUseCase, UploadAlertImageUseCase {

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    private final AlertRepositoryPort alertRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final CropRepositoryPort cropRepositoryPort;
    private final AnimalRepositoryPort animalRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public AlertService(AlertRepositoryPort alertRepositoryPort, UserRepositoryPort userRepositoryPort,
                        LotRepositoryPort lotRepositoryPort, CropRepositoryPort cropRepositoryPort,
                        AnimalRepositoryPort animalRepositoryPort,
                        FileStoragePort fileStoragePort,
                        org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.alertRepositoryPort = alertRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.cropRepositoryPort = cropRepositoryPort;
        this.animalRepositoryPort = animalRepositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public Alert execute(String title, AlertType alertType, Priority priority, RecordType recordType,
                         String description, LocalDateTime createdAt, UUID authorId, List<String> images,
                         UUID relatedLotId, UUID relatedCropId, UUID relatedAnimalId,
                         Polygon polygonLimit, Point centroid) {

        // 1. Buscar al autor
        if (authorId == null) {
            throw new BusinessRuleViolationsException("El ID del autor no puede ser nulo");
        }
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
    public String executeUploadAlertImage(UUID farmId, String originalFilename, String contentType, long size, InputStream inputStream) {
        if (farmId == null) {
            throw new BusinessRuleViolationsException("El ID de la finca no puede ser nulo");
        }
        if (inputStream == null || size <= 0) {
            throw new BusinessRuleViolationsException("El archivo de imagen no puede estar vacío");
        }
        if (size > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessRuleViolationsException("El archivo supera el tamaño máximo permitido de 10MB");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessRuleViolationsException("El tipo de contenido de la imagen es obligatorio");
        }

        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        String ext = extractLowercaseExtension(originalFilename);

        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
        byte[] header;
        try {
            bufferedStream.mark(16);
            header = bufferedStream.readNBytes(12);
            if (header.length == 0) {
                throw new BusinessRuleViolationsException("El archivo de imagen no puede estar vacío");
            }
            bufferedStream.reset();
        } catch (IOException e) {
            throw new BusinessRuleViolationsException("Error al validar el contenido de la imagen");
        }

        String safeExtension = switch (normalizedContentType) {
            case "image/jpeg" -> {
                if (!"jpg".equals(ext) && !"jpeg".equals(ext)) {
                    throw new BusinessRuleViolationsException("La extensión del archivo no coincide con image/jpeg (.jpg o .jpeg)");
                }
                if (!isJpegMagic(header)) {
                    throw new BusinessRuleViolationsException("El contenido real del archivo no corresponde a una imagen JPEG válida");
                }
                yield "jpeg".equals(ext) ? "jpeg" : "jpg";
            }
            case "image/png" -> {
                if (!"png".equals(ext)) {
                    throw new BusinessRuleViolationsException("La extensión del archivo no coincide con image/png (.png)");
                }
                if (!isPngMagic(header)) {
                    throw new BusinessRuleViolationsException("El contenido real del archivo no corresponde a una imagen PNG válida");
                }
                yield "png";
            }
            case "image/webp" -> {
                if (!"webp".equals(ext)) {
                    throw new BusinessRuleViolationsException("La extensión del archivo no coincide con image/webp (.webp)");
                }
                if (!isWebpMagic(header)) {
                    throw new BusinessRuleViolationsException("El contenido real del archivo no corresponde a una imagen WEBP válida");
                }
                yield "webp";
            }
            default -> throw new BusinessRuleViolationsException(
                    "Formato de imagen no permitido. Solo se aceptan JPEG (.jpg, .jpeg), PNG (.png) y WEBP (.webp)");
        };

        // Clave limpia sin usar el nombre original del cliente (evita Path Traversal) y sin hostnames internos acoplados
        String objectKey = "novedades/" + farmId + "/" + UUID.randomUUID() + "." + safeExtension;
        fileStoragePort.uploadFile(objectKey, bufferedStream, size, normalizedContentType);
        return objectKey;
    }

    private String extractLowercaseExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessRuleViolationsException("El archivo debe incluir una extensión válida (.jpg, .jpeg, .png, .webp)");
        }
        int lastSlash = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
        String baseName = (lastSlash >= 0) ? originalFilename.substring(lastSlash + 1) : originalFilename;
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == baseName.length() - 1) {
            throw new BusinessRuleViolationsException("El archivo debe incluir una extensión válida (.jpg, .jpeg, .png, .webp)");
        }
        return baseName.substring(lastDot + 1).trim().toLowerCase(Locale.ROOT);
    }

    private boolean isJpegMagic(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private boolean isPngMagic(byte[] header) {
        return header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private boolean isWebpMagic(byte[] header) {
        return header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
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

    @Override
    @Transactional
    public void executeDeleteAlert(UUID alertId) {
        Alert alert = alertRepositoryPort.findById(alertId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Alerta no encontrada con ID: " + alertId));
        
        alert.setActive(false);
        alertRepositoryPort.save(alert);
    }
}
