package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.model.enums.UserRole;
import com.agrotrack.domain.model.enums.SoilType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnimalMovementTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Lot originLot;
    private User registeredBy;

    @BeforeEach
    void setUp() {
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        originLot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);
        
        registeredBy = User.create("Pepe", "Perez", "12345678", "555-1234", "Addr", "pepe@test.com", new Password("Password987!"));
    }

    @Test
    void testCreateAnimalMovementSuccess() {
        LocalDateTime entryDate = LocalDateTime.now().minusDays(5);
        AnimalMovement movement = AnimalMovement.create(originLot, entryDate, registeredBy);
        
        assertNotNull(movement);
        assertEquals(originLot, movement.getOriginLot());
        assertEquals(entryDate, movement.getEntryDate());
        assertEquals(registeredBy, movement.getRegisteredBy());
        assertTrue(movement.isActive());
        assertNull(movement.getExitDate());
    }

    @Test
    void testCreateAnimalMovementThrowsExceptionWhenFutureDate() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            AnimalMovement.create(originLot, LocalDateTime.now().plusDays(2), registeredBy)
        );
    }

    @Test
    void testCloseMovementSuccess() {
        LocalDateTime entryDate = LocalDateTime.now().minusDays(5);
        AnimalMovement movement = AnimalMovement.create(originLot, entryDate, registeredBy);
        
        LocalDateTime exitDate = LocalDateTime.now().minusDays(1);
        movement.closeMovement(exitDate);
        
        assertFalse(movement.isActive());
        assertEquals(exitDate, movement.getExitDate());
    }

    @Test
    void testCloseMovementThrowsExceptionWhenExitBeforeEntry() {
        LocalDateTime entryDate = LocalDateTime.now().minusDays(2);
        AnimalMovement movement = AnimalMovement.create(originLot, entryDate, registeredBy);
        
        assertThrows(BusinessRuleViolationsException.class, () -> 
            movement.closeMovement(entryDate.minusDays(1))
        );
    }

    @Test
    void testGetDurationInDays() {
        LocalDateTime entryDate = LocalDateTime.now().minusDays(10);
        AnimalMovement movement = AnimalMovement.create(originLot, entryDate, registeredBy);
        
        LocalDateTime exitDate = LocalDateTime.now().minusDays(2);
        movement.closeMovement(exitDate);
        
        assertEquals(8, movement.getDurationInDays(LocalDateTime.now()));
    }
}
