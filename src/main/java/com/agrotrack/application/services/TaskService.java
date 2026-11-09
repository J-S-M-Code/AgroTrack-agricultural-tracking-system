package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.in.task.CreateTaskUseCase;
import com.agrotrack.domain.port.in.task.DeleteTaskUseCase;
import com.agrotrack.domain.port.in.task.GetAssignedTasksUseCase;
import com.agrotrack.domain.port.in.task.GetTaskByIdUseCase;
import com.agrotrack.domain.port.in.task.GetTasksByFarmUseCase;
import com.agrotrack.domain.port.in.task.UpdateTaskStatusUseCase;
import com.agrotrack.domain.port.in.task.UploadTaskImageUseCase;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
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
public class TaskService implements CreateTaskUseCase, UpdateTaskStatusUseCase, GetTasksByFarmUseCase, GetTaskByIdUseCase, GetAssignedTasksUseCase, DeleteTaskUseCase, UploadTaskImageUseCase {

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    private final TaskRepositoryPort taskRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final FarmRepositoryPort farmRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final ApplicationDtoMapper applicationDtoMapper;

    public TaskService(TaskRepositoryPort taskRepositoryPort, UserRepositoryPort userRepositoryPort,
                       FarmRepositoryPort farmRepositoryPort, LotRepositoryPort lotRepositoryPort,
                       FileStoragePort fileStoragePort,
                       ApplicationDtoMapper applicationDtoMapper) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.farmRepositoryPort = farmRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.fileStoragePort = fileStoragePort;
        this.applicationDtoMapper = applicationDtoMapper;
    }

    @Override
    @Transactional
    public Task executeCreateTask(String title, AccionType accionType, TaskStatus taskStatus, LocalDateTime dueDate,
                        Priority priority, LocalDateTime creationDate, LocalDateTime completeDate,
                        UUID creatorId, UUID assignedId, UUID relatedFarmId, UUID relatedLotId,
                        Polygon polygonLimit, Point centroid, List<String> images, String description) {

        // 1. Buscar a los usuarios involucrados
        User creator = userRepositoryPort.findById(creatorId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Creador no encontrado"));
        User assigned = userRepositoryPort.findById(assignedId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Usuario asignado no encontrado"));

        // 2. Buscar Finca y Lote (El lote es opcional, puede ser una tarea para toda la finca)
        Farm farm = farmRepositoryPort.findById(relatedFarmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada"));
        Lot lot = (relatedLotId != null)
                ? lotRepositoryPort.findById(relatedLotId).orElse(null)
                : null;

        // 3. Crear la tarea (El dominio validará fechas, nulos, y asignará polígonos automáticamente si vienen vacíos)
        Task newTask = Task.create(
                title, accionType, taskStatus, dueDate, priority, creationDate, completeDate,
                creator, assigned, farm, lot, polygonLimit, centroid, images, description
        );

        // 4. Agregar a la lista de la finca y persistir
        farm.newTask(newTask);

        farmRepositoryPort.save(farm); // Actualiza la relación de la finca
        return taskRepositoryPort.save(newTask);
    }

    @Override
    @Transactional
    public TaskDto executeUpdateTaskStatus(UUID taskId, TaskStatus newStatus, String completionNotes, List<String> completionImages) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Tarea no encontrada"));

        task.updateStatus(newStatus, completionNotes, completionImages);
        Task savedTask = taskRepositoryPort.save(task);
        return applicationDtoMapper.toTaskDto(savedTask);
    }

    @Override
    public String executeUploadTaskImage(UUID farmId, String originalFilename, String contentType, long size, InputStream inputStream) {
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

        String objectKey = "tareas/" + farmId + "/" + UUID.randomUUID() + "." + safeExtension;
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
    public List<TaskDto> executeGetTasksByFarm(UUID farmId) {
        List<Task> tasks = taskRepositoryPort.findByFarmId(farmId);
        return applicationDtoMapper.toTaskDtoList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto executeGetTaskById(UUID taskId) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Tarea no encontrada con ID: " + taskId));
        return applicationDtoMapper.toTaskDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> executeGetAssignedTasks(UUID userId) {
        List<Task> tasks = taskRepositoryPort.findByAssignedUserId(userId);
        return applicationDtoMapper.toTaskDtoList(tasks);
    }

    @Override
    @Transactional
    public void executeDeleteTask(UUID taskId, String reason) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Tarea no encontrada con ID: " + taskId));
        
        task.setActive(false);
        task.setDeletionReason(reason);
        taskRepositoryPort.save(task);
    }
}
