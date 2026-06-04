package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.TaskDto;
// import com.agrotrack.domain.port.in.task.CreateTaskUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    // private final CreateTaskUseCase createTaskUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FOREMAN', 'AGRONOMIST', 'VETERINARIAN')")
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) {
        // AGRONOMIST crea tareas para APPLICATOR
        // VETERINARIAN crea tareas relacionadas con animales
        // OWNER / FOREMAN pueden crear tareas generales para WORKER
        return ResponseEntity.ok(taskDto); // Simulación
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('OWNER', 'AGRONOMIST', 'FOREMAN', 'APPLICATOR', 'VETERINARIAN', 'WORKER')")
    public ResponseEntity<List<TaskDto>> getAssignedTasks() {
        // Obtiene las tareas asignadas al usuario autenticado (extraído del token)
        return ResponseEntity.ok(List.of()); 
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('APPLICATOR', 'VETERINARIAN', 'WORKER', 'OWNER', 'FOREMAN')")
    public ResponseEntity<Void> updateTaskStatus(@PathVariable UUID taskId, @RequestParam String newStatus) {
        // Un WORKER / APPLICATOR marca su tarea como completada
        return ResponseEntity.ok().build();
    }
}
