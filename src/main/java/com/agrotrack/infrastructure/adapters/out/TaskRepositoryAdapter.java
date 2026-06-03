package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import com.agrotrack.infrastructure.adapters.out.database.entities.FarmJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.LotJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.TaskJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.entities.UserJpaEntity;
import com.agrotrack.infrastructure.adapters.out.database.repositories.TaskJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;

    public TaskRepositoryAdapter(TaskJpaRepository taskJpaRepository) {
        this.taskJpaRepository = taskJpaRepository;
    }

    @Override
    public Task save(Task task) {
        // 1. Mapear Usuario Creador (Obligatorio)
        UserJpaEntity creatorEntity = new UserJpaEntity();
        creatorEntity.setId(task.getCreator().getIdUser());

        // 2. Mapear Usuario Asignado (Puede ser nulo)
        UserJpaEntity assignedEntity = null;
        if (task.getAssigned() != null) {
            assignedEntity = new UserJpaEntity();
            assignedEntity.setId(task.getAssigned().getIdUser());
        }

        // 3. Mapear Finca (Puede ser nula dependiendo de la tarea)
        FarmJpaEntity farmEntity = null;
        if (task.getRelatedFarm() != null) {
            farmEntity = new FarmJpaEntity();
            farmEntity.setId(task.getRelatedFarm().getIdFarm());
        }

        // 4. Mapear Lote (Puede ser nulo)
        LotJpaEntity lotEntity = null;
        if (task.getRelatedLot() != null) {
            lotEntity = new LotJpaEntity();
            lotEntity.setId(task.getRelatedLot().getIdLot());
        }

        // 5. Construir la entidad JPA
        TaskJpaEntity entity = new TaskJpaEntity(
                task.getIdTask(),
                task.getTitle(),
                task.getAccionType(),
                task.getTaskStatus(),
                task.getDueDate(),
                task.getPriority(),
                task.getCreationDate(),
                task.getCompleteDate(),
                creatorEntity,
                assignedEntity,
                farmEntity,
                lotEntity,
                task.getPolygonLimit(),
                task.getCentroid(),
                task.getImages() != null ? new ArrayList<>(task.getImages()) : new ArrayList<>(),
                task.getDescription()
        );

        // 6. Guardar en Base de Datos y asignar UUID
        TaskJpaEntity savedEntity = taskJpaRepository.save(entity);
        task.setIdTask(savedEntity.getId());

        return task;
    }

    @Override
    public Optional<Task> findById(UUID taskId) {
        return taskJpaRepository.findById(taskId).map(this::mapToDomain);
    }

    @Override
    public List<Task> findByFarmId(UUID farmId) {
        return taskJpaRepository.findByFarmId(farmId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByAssignedUserIdAndStatus(UUID assignedUserId, TaskStatus status) {
        return taskJpaRepository.findByAssignedIdAndTaskStatus(assignedUserId, status).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    // --- Método auxiliar para convertir de Base de Datos a Dominio ---
    private Task mapToDomain(TaskJpaEntity entity) {

        // Reconstruir Creador
        User creator = User.create(
                entity.getCreator().getName(), entity.getCreator().getLastName(),
                entity.getCreator().getDni(), entity.getCreator().getPhone(),
                entity.getCreator().getAddress(), entity.getCreator().getEmail(),
                new Password("DUMMY123*"), entity.getCreator().getRole()
        );
        creator.setIdUser(entity.getCreator().getId());

        // Reconstruir Asignado (si existe)
        User assigned = null;
        if (entity.getAssigned() != null) {
            assigned = User.create(
                    entity.getAssigned().getName(), entity.getAssigned().getLastName(),
                    entity.getAssigned().getDni(), entity.getAssigned().getPhone(),
                    entity.getAssigned().getAddress(), entity.getAssigned().getEmail(),
                    new Password("DUMMY123*"), entity.getAssigned().getRole()
            );
            assigned.setIdUser(entity.getAssigned().getId());
        }

        // Reconstruir Finca (si existe)
        Farm farm = null;
        if (entity.getFarm() != null) {
            farm = Farm.create(
                    entity.getFarm().getName(), entity.getFarm().getCompanyName(),
                    entity.getFarm().getCuit(), entity.getFarm().getNumberRENAPSA(),
                    entity.getFarm().getProductiveOrientation(), entity.getFarm().getAddress(),
                    entity.getFarm().getPolygonLimit(), entity.getFarm().getCentroid(),
                    entity.getFarm().getSurface(), entity.getFarm().getImageUrl()
            );
            farm.setIdFarm(entity.getFarm().getId());
        }

        // Reconstruir Lote (si existe)
        Lot lot = null;
        if (entity.getLot() != null) {
            lot = Lot.create(
                    entity.getLot().getName(), entity.getLot().getHectares(),
                    entity.getLot().getSoilType(), entity.getLot().getType(),
                    entity.getLot().getDescription(), entity.getLot().getPolygonLimit(), farm
            );
            lot.setIdLot(entity.getLot().getId());
        }

        // Reconstruir la Tarea Final
        Task task = Task.create(
                entity.getTitle(), entity.getAccionType(), entity.getTaskStatus(),
                entity.getDueDate(), entity.getPriority(), entity.getCreationDate(),
                entity.getCompleteDate(), creator, assigned, farm, lot,
                entity.getPolygonLimit(), entity.getCentroid(),
                new ArrayList<>(entity.getImages()), entity.getDescription()
        );
        task.setIdTask(entity.getId());

        return task;
    }
}