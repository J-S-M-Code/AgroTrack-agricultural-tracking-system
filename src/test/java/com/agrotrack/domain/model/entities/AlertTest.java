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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Lot assignedLot;
    private Crop assignedCrop;
    private User author;

    @BeforeEach
    void setUp() {
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        assignedLot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);

        assignedCrop = Crop.create(
                TypeCrop.CEREAL, "Trigo", "Baguette",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().plusMonths(3),
                assignedLot, 5.0, "Renspa1", PhenologicalState.GERMINATION, false
        );
        
        author = User.create("Pepe", "Perez", "12345678", "555-1234", "Addr", "pepe@test.com", new Password("Password987!"));
    }

    @Test
    void testCreateAlertSuccessWithLotInheritsCentroidWhenNull() {
        Alert alert = Alert.create(
                "Vaca perdida", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Se salió del corral", LocalDateTime.now(), author, new ArrayList<>(), assignedLot, null, null, null, null
        );

        assertNotNull(alert);
        assertEquals("Vaca perdida", alert.getTitle());
        assertEquals(Priority.HIGH, alert.getPriority());
        assertEquals(author, alert.getAuthor());
        assertNotNull(alert.getPolygonLimit());
        assertNotNull(alert.getCentroid());
        assertEquals(assignedLot.getPolygonLimit().getCentroid().getX(), alert.getCentroid().getX(), 1e-6);
        assertEquals(assignedLot.getPolygonLimit().getCentroid().getY(), alert.getCentroid().getY(), 1e-6);
    }

    @Test
    void testCreateAlertPreservesExactGpsCentroidWhenPolygonIsNullOnLot() {
        Point exactGpsPoint = geometryFactory.createPoint(new Coordinate(-67.498312, -29.161045));
        List<String> images = List.of("novedades/123e4567-e89b-12d3-a456-426614174000/foto1.jpg");

        Alert alert = Alert.create(
                "Plaga detectada en campo", AlertType.MANUAL_NOTE, Priority.HIGH, RecordType.ALERT,
                "Foto tomada con celular", LocalDateTime.now(), author, images, assignedLot, null, null, null, exactGpsPoint
        );

        assertNotNull(alert.getPolygonLimit());
        assertEquals(assignedLot.getPolygonLimit(), alert.getPolygonLimit());
        assertNotNull(alert.getCentroid());
        assertEquals(-67.498312, alert.getCentroid().getX(), 1e-7);
        assertEquals(-29.161045, alert.getCentroid().getY(), 1e-7);
        assertEquals(1, alert.getImages().size());
    }

    @Test
    void testCreateAlertPreservesExactGpsCentroidWhenPolygonIsNullOnCrop() {
        Point exactGpsPoint = geometryFactory.createPoint(new Coordinate(-67.501111, -29.170222));

        Alert alert = Alert.create(
                "Roya en trigo", AlertType.MANUAL_NOTE, Priority.MEDIUM, RecordType.NOTE,
                "Muestra puntual GPS", LocalDateTime.now(), author, List.of(), null, assignedCrop, null, null, exactGpsPoint
        );

        assertNotNull(alert.getPolygonLimit());
        assertEquals(assignedLot.getPolygonLimit(), alert.getPolygonLimit());
        assertNotNull(alert.getCentroid());
        assertEquals(-67.501111, alert.getCentroid().getX(), 1e-7);
        assertEquals(-29.170222, alert.getCentroid().getY(), 1e-7);
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
