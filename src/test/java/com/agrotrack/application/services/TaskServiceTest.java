package com.agrotrack.application.services;

import com.agrotrack.application.dto.TaskDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.Task;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import com.agrotrack.domain.port.out.task.TaskRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private FarmRepositoryPort farmRepositoryPort;
    @Mock
    private LotRepositoryPort lotRepositoryPort;
    @Mock
    private FileStoragePort fileStoragePort;
    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @InjectMocks
    private TaskService taskService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void executeCreateTask_Success() {
        // Arrange
        UUID creatorId = UUID.randomUUID();
        UUID assignedId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();

        User creator = User.create("Juan", "P", "1", "1", "D", "e@e.com", new Password("SecurePass1!"));
        User assigned = User.create("Ana", "G", "2", "2", "D", "a@a.com", new Password("SecurePass1!"));
        
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");

        when(userRepositoryPort.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepositoryPort.findById(assignedId)).thenReturn(Optional.of(assigned));
        when(farmRepositoryPort.findById(farmId)).thenReturn(Optional.of(farm));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            t.setIdTask(UUID.randomUUID());
            return t;
        });

        LocalDateTime now = LocalDateTime.now();

        // Act
        Task task = taskService.executeCreateTask("Siembra", AccionType.FERTILIZATION, TaskStatus.CREATED, now.plusDays(1), Priority.HIGH, now, null, creatorId, assignedId, farmId, null, null, null, null, "Desc");

        // Assert
        assertNotNull(task.getIdTask());
        assertEquals("Siembra", task.getTitle());
        verify(taskRepositoryPort).save(any(Task.class));
        verify(farmRepositoryPort).save(farm);
    }

    @Test
    void executeUpdateTaskStatus_WithCompletionEvidence_Success() {
        UUID taskId = UUID.randomUUID();
        User creator = User.create("Juan", "P", "1", "1", "D", "e@e.com", new Password("SecurePass1!"));
        User assigned = User.create("Ana", "G", "2", "2", "D", "a@a.com", new Password("SecurePass1!"));
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");

        LocalDateTime now = LocalDateTime.now();
        Task task = Task.create("Fumigación", AccionType.FUMIGATION, TaskStatus.CREATED, now.plusDays(1), Priority.HIGH, now, null, creator, assigned, farm, null, null, null, null, "Desc");
        task.setIdTask(taskId);

        when(taskRepositoryPort.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepositoryPort.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        when(applicationDtoMapper.toTaskDto(any(Task.class))).thenAnswer(i -> {
            Task t = i.getArgument(0);
            return TaskDto.builder()
                    .idTask(t.getIdTask())
                    .taskStatus(t.getTaskStatus())
                    .completionNotes(t.getCompletionNotes())
                    .completionImages(t.getCompletionImages())
                    .build();
        });

        TaskDto updated = taskService.executeUpdateTaskStatus(taskId, TaskStatus.FINISH, "Labor finalizada ok", List.of("tareas/f1/foto1.jpg"));

        assertNotNull(updated);
        assertEquals(TaskStatus.FINISH, updated.getTaskStatus());
        assertEquals("Labor finalizada ok", updated.getCompletionNotes());
        assertEquals(List.of("tareas/f1/foto1.jpg"), updated.getCompletionImages());
        assertNotNull(task.getCompleteDate());
    }

    @Test
    void executeUploadTaskImage_ValidJpeg_Success() {
        UUID farmId = UUID.randomUUID();
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01};
        ByteArrayInputStream bais = new ByteArrayInputStream(jpegHeader);

        String path = taskService.executeUploadTaskImage(farmId, "evidencia.jpg", "image/jpeg", jpegHeader.length, bais);

        assertNotNull(path);
        assertTrue(path.startsWith("tareas/" + farmId + "/"));
        assertTrue(path.endsWith(".jpg"));
        verify(fileStoragePort).uploadFile(eq(path), any(), eq((long) jpegHeader.length), eq("image/jpeg"));
    }
}
