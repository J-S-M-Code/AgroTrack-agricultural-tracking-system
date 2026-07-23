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
import com.agrotrack.domain.port.in.task.GetTaskByIdUseCase;
import com.agrotrack.domain.port.in.task.GetAssignedTasksUseCase;
import com.agrotrack.domain.port.in.task.UpdateTaskStatusUseCase;
import com.agrotrack.domain.port.in.task.GetTasksByFarmUseCase;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.agrotrack.domain.port.in.task.DeleteTaskUseCase;

@Service
public class TaskService implements CreateTaskUseCase, UpdateTaskStatusUseCase, GetTasksByFarmUseCase, GetTaskByIdUseCase, GetAssignedTasksUseCase, DeleteTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final FarmRepositoryPort farmRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final ApplicationDtoMapper applicationDtoMapper;

    public TaskService(TaskRepositoryPort taskRepositoryPort, UserRepositoryPort userRepositoryPort,
                       FarmRepositoryPort farmRepositoryPort, LotRepositoryPort lotRepositoryPort,
                       ApplicationDtoMapper applicationDtoMapper) {
        this.taskRepositoryPort = taskRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.farmRepositoryPort = farmRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
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
    public void executeUpdateTaskStatus(UUID taskId, TaskStatus newStatus) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Tarea no encontrada"));

        task.updateStatus(newStatus);
        taskRepositoryPort.save(task);
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