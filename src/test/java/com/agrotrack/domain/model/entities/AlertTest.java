package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Lot assignedLot;
    private User author;

    @BeforeEach
    void setUp() {
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        assignedLot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", lotPolygon, farm);
        
        author = User.create("Pepe", "Perez", "12345678", "555-1234", "Addr", "pepe@test.com", new Password("Password987!"), UserRole.WORKER);
    }

    @Test
    void testCreateAlertSuccessWithLot() {
        Point centroid = geometryFactory.createPoint(new Coordinate(2, 2));

        Alert alert = Alert.create(
                "Vaca perdida", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Se salió del corral", LocalDateTime.now(), author, new ArrayList<>(), assignedLot, null, null, null, null
        );

        assertNotNull(alert);
        assertEquals("Vaca perdida", alert.getTitle());
        assertEquals(Priority.HIGH, alert.getPriority());
        assertEquals(author, alert.getAuthor());
        assertNotNull(alert.getPolygonLimit()); // Should be inherited from assignedLot
        assertNotNull(alert.getCentroid());
    }

    @Test
    void testCreateAlertThrowsExceptionWhenNoRelatedEntity() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            Alert.create(
                "Vaca perdida", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Se salió del corral", LocalDateTime.now(), author, new ArrayList<>(), null, null, null, null, null
            )
        );
    }

    @Test
    void testCreateAlertThrowsExceptionWhenTitleBlank() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            Alert.create(
                "", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Se salió del corral", LocalDateTime.now(), author, new ArrayList<>(), assignedLot, null, null, null, null
            )
        );
    }
}
