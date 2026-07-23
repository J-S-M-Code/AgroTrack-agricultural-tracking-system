package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.in.task.CreateTaskUseCase;
import com.agrotrack.domain.port.in.task.GetAssignedTasksUseCase;
import com.agrotrack.domain.port.in.task.GetTaskByIdUseCase;
import com.agrotrack.domain.port.in.task.GetTasksByFarmUseCase;
import com.agrotrack.domain.port.in.task.UpdateTaskStatusUseCase;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetAssignedTasksUseCase getAssignedTasksUseCase;
    private final GetTaskByIdUseCase getTaskByIdUseCase;
    private final GetTasksByFarmUseCase getTasksByFarmUseCase;
    private final UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private final com.agrotrack.domain.port.in.task.DeleteTaskUseCase deleteTaskUseCase;
    private final com.agrotrack.application.mapper.ApplicationDtoMapper mapper;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          GetAssignedTasksUseCase getAssignedTasksUseCase,
                          GetTaskByIdUseCase getTaskByIdUseCase,
                          GetTasksByFarmUseCase getTasksByFarmUseCase,
                          UpdateTaskStatusUseCase updateTaskStatusUseCase,
                          com.agrotrack.domain.port.in.task.DeleteTaskUseCase deleteTaskUseCase,
                          com.agrotrack.application.mapper.ApplicationDtoMapper mapper) {
        this.createTaskUseCase = createTaskUseCase;
        this.getAssignedTasksUseCase = getAssignedTasksUseCase;
        this.getTaskByIdUseCase = getTaskByIdUseCase;
        this.getTasksByFarmUseCase = getTasksByFarmUseCase;
        this.updateTaskStatusUseCase = updateTaskStatusUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN', 'AGRONOMIST', 'VETERINARIAN')")
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
                null, // polygonLimit: Se podría mapear si se usa
                null, // centroid
                taskDto.getImages(),
                taskDto.getDescription()
        );
        return ResponseEntity.ok(mapper.toTaskDto(createdTask));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<TaskDto>> getAssignedTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TaskDto> dtos = getAssignedTasksUseCase.executeGetAssignedTasks(userDetails.getUser().getIdUser());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<TaskDto>> getTasksByFarm(@PathVariable UUID farmId) {
        List<TaskDto> dtos = getTasksByFarmUseCase.executeGetTasksByFarm(farmId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(getTaskByIdUseCase.executeGetTaskById(id));
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('APPLICATOR', 'VETERINARIAN', 'WORKER', 'OWNER', 'FOREMAN')")
    public ResponseEntity<Void> updateTaskStatus(@PathVariable UUID taskId, @RequestParam TaskStatus newStatus) {
        updateTaskStatusUseCase.executeUpdateTaskStatus(taskId, newStatus);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @RequestParam(required = false) String reason) {
        deleteTaskUseCase.executeDeleteTask(taskId, reason);
        return ResponseEntity.ok().build();
    }
}
