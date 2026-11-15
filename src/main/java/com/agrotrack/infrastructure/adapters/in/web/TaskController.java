package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.application.dto.TaskStatusUpdateDto;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.in.task.CreateTaskUseCase;
import com.agrotrack.domain.port.in.task.DeleteTaskUseCase;
import com.agrotrack.domain.port.in.task.GetAssignedTasksUseCase;
import com.agrotrack.domain.port.in.task.GetTaskByIdUseCase;
import com.agrotrack.domain.port.in.task.GetTasksByFarmUseCase;
import com.agrotrack.domain.port.in.task.UpdateTaskStatusUseCase;
import com.agrotrack.domain.port.in.task.UploadTaskImageUseCase;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetAssignedTasksUseCase getAssignedTasksUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final GetTasksByFarmUseCase getTasksByFarmUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final UploadTaskImageUseCase uploadTaskImageUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          GetAssignedTasksUseCase getAssignedTasksUseCase,
                          GetTaskByIdUseCase getTaskByIdUseCase,
                          GetTasksByFarmUseCase getTasksByFarmUseCase,
                          UpdateTaskStatusUseCase updateTaskStatusUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          UploadTaskImageUseCase uploadTaskImageUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper,
                          SimpMessagingTemplate messagingTemplate) {
        this.createTaskUseCase = createTaskUseCase;
        this.getAssignedTasksUseCase = getAssignedTasksUseCase;
        this.getTaskByIdUseCase = getTaskByIdUseCase;
        this.getTasksByFarmUseCase = getTasksByFarmUseCase;
        this.updateTaskStatusUseCase = updateTaskStatusUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.uploadTaskImageUseCase = uploadTaskImageUseCase;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/images")
    @PreAuthorize("hasPermission(#farmId, 'ANY')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("farmId") UUID farmId) throws IOException {
        try (InputStream is = file.getInputStream()) {
            String objectKey = uploadTaskImageUseCase.executeUploadTaskImage(
                    farmId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    is
            );
            return ResponseEntity.ok(Map.of("path", objectKey));
        }
    }

    @PostMapping
    @PreAuthorize("hasPermission(#taskDto.relatedFarmId, 'VETERINARIAN')") // VETERINARIAN or higher (AGRONOMIST, FOREMAN, OWNER)
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Task createdTask = createTaskUseCase.executeCreateTask(
                taskDto.getTitle(),
                taskDto.getAccionType(),
                taskDto.getTaskStatus(),
                taskDto.getDueDate(),
                taskDto.getPriority(),
                taskDto.getCreationDate(),
                taskDto.getCompleteDate(),
                userDetails.getUser().getIdUser(), // Asignamos al usuario autenticado como creador
                taskDto.getAssignedId(),
                taskDto.getRelatedFarmId(),
                taskDto.getRelatedLotId(),
                mapper.mapPolygonDto(taskDto.getPolygonLimit()),
                mapper.mapPointDto(taskDto.getCentroid()),
                taskDto.getImages(),
                taskDto.getDescription()
        );
        TaskDto createdDto = mapper.toTaskDto(createdTask);
        if (createdDto != null && createdDto.getRelatedFarmId() != null) {
            messagingTemplate.convertAndSend("/topic/farms/" + createdDto.getRelatedFarmId() + "/tasks", createdDto);
        }
        return ResponseEntity.ok(createdDto);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasPermission('ANY_FARM', 'ANY')")
    public ResponseEntity<List<TaskDto>> getAssignedTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TaskDto> dtos = getAssignedTasksUseCase.executeGetAssignedTasks(userDetails.getUser().getIdUser());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasPermission(#farmId, 'ANY')")
    public ResponseEntity<List<TaskDto>> getTasksByFarm(@PathVariable UUID farmId) {
        List<TaskDto> dtos = getTasksByFarmUseCase.executeGetTasksByFarm(farmId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('ANY_FARM', 'ANY')")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(getTaskByIdUseCase.executeGetTaskById(id));
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasPermission('ANY_FARM', 'ANY')")
    public ResponseEntity<Void> updateTaskStatus(
            @PathVariable UUID taskId,
            @RequestParam TaskStatus newStatus,
            @RequestBody(required = false) TaskStatusUpdateDto body) {
        String completionNotes = body != null ? body.getCompletionNotes() : null;
        List<String> completionImages = body != null ? body.getCompletionImages() : null;
        TaskDto updatedDto = updateTaskStatusUseCase.executeUpdateTaskStatus(taskId, newStatus, completionNotes, completionImages);
        if (updatedDto != null && updatedDto.getRelatedFarmId() != null) {
            messagingTemplate.convertAndSend("/topic/farms/" + updatedDto.getRelatedFarmId() + "/tasks", updatedDto);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasPermission(#farmId, 'FOREMAN')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @RequestParam(required = false) String reason, @RequestParam UUID farmId) {
        deleteTaskUseCase.executeDeleteTask(taskId, reason);
        return ResponseEntity.ok().build();
    }
}
