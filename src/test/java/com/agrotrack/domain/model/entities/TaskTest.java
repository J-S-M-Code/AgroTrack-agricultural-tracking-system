package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.AccionType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.TaskStatus;
import com.agrotrack.domain.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void testCreateTaskSuccess() {
        // Arrange
        User creator = User.create("Juan", "Perez", "123", "123", "Dir", "c@c.com", new Password("SecurePass1!"), UserRole.OWNER);
        User assigned = User.create("Ana", "Gomez", "456", "456", "Dir", "a@c.com", new Password("SecurePass1!"), UserRole.WORKER);
        
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 100.0, "url");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(2);

        // Act
        Task task = Task.create(
                "Siembra",
                AccionType.FERTILIZATION,
                TaskStatus.CREATED,
                due,
                Priority.HIGH,
                now,
                null,
                creator,
                assigned,
                farm,
                null,
                null,
                null,
                null,
                "Tarea de siembra"
        );

        // Assert
        assertNotNull(task);
        assertEquals("Siembra", task.getTitle());
        assertEquals(TaskStatus.CREATED, task.getTaskStatus());
        assertEquals(creator, task.getCreator());
        assertEquals(assigned, task.getAssigned());
        assertEquals(farm, task.getRelatedFarm());
        // Since polygonLimit was null, it should take it from relatedFarm
        assertNotNull(task.getPolygonLimit());
        assertEquals(farm.getPolygonLimit(), task.getPolygonLimit());
    }

    @Test
    void testUpdateStatus() {
        User creator = User.create("Juan", "Perez", "123", "123", "Dir", "c@c.com", new Password("SecurePass1!"), UserRole.OWNER);
        User assigned = User.create("Ana", "Gomez", "456", "456", "Dir", "a@c.com", new Password("SecurePass1!"), UserRole.WORKER);
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 100.0, "url");

        LocalDateTime now = LocalDateTime.now();
        Task task = Task.create("Siembra", AccionType.FERTILIZATION, TaskStatus.CREATED, now.plusDays(1), Priority.HIGH, now, null, creator, assigned, farm, null, null, null, null, "Desc");

        task.updateStatus(TaskStatus.IN_PROGESS);
        assertEquals(TaskStatus.IN_PROGESS, task.getTaskStatus());
    }

    @Test
    void testCreateTaskThrowsExceptionWhenDueDateBeforeCreation() {
        User creator = User.create("Juan", "Perez", "123", "123", "Dir", "c@c.com", new Password("SecurePass1!"), UserRole.OWNER);
        User assigned = User.create("Ana", "Gomez", "456", "456", "Dir", "a@c.com", new Password("SecurePass1!"), UserRole.WORKER);
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 100.0, "url");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastDue = now.minusDays(1);

        assertThrows(BusinessRuleViolationsException.class, () -> 
            Task.create("Siembra", AccionType.FERTILIZATION, TaskStatus.CREATED, pastDue, Priority.HIGH, now, null, creator, assigned, farm, null, null, null, null, "Desc")
        );
    }
}
