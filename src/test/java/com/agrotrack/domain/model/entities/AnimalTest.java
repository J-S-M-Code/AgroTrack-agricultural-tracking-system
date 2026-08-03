package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.Sex;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.model.enums.Species;
import com.agrotrack.domain.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Lot assignedLot;

    @BeforeEach
    void setUp() {
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        assignedLot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);
    }

    @Test
    void testCreateAnimalSuccess() {
        LocalDateTime birthdate = LocalDateTime.now().minusMonths(12);

        Animal animal = Animal.create(
                "Vis123", "Senasa123", "Key123", "Renspa123", "Int123",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.STEER,
                birthdate, 300.5, assignedLot
        );

        assertNotNull(animal);
        assertEquals("Vis123", animal.getVisualCaravan());
        assertEquals(Species.BOVINE, animal.getSpecies());
        assertEquals(Sex.MALE, animal.getSex());
        assertEquals(CategoryAnimal.STEER, animal.getCategory());
        assertEquals(300.5, animal.getCurrentWeight());
        assertEquals(assignedLot, animal.getAssignedLot());
    }

    @Test
    void testCreateAnimalThrowsExceptionWhenVisualCaravanIsBlank() {
        LocalDateTime birthdate = LocalDateTime.now().minusMonths(12);

        assertThrows(BusinessRuleViolationsException.class, () -> 
            Animal.create(
                "", "Senasa123", "Key123", "Renspa123", "Int123",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.STEER,
                birthdate, 300.5, assignedLot
            )
        );
    }

    @Test
    void testCalculateAgeInMonths() {
        LocalDateTime birthdate = LocalDateTime.now().minusMonths(15).minusDays(5);
        Animal animal = Animal.create(
                "Vis123", "Senasa123", "Key123", "Renspa123", "Int123",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.STEER,
                birthdate, 300.5, assignedLot
        );

        int age = animal.calculateAgeInMonths(LocalDateTime.now());
        assertEquals(15, age);
    }

    @Test
    void testUpdateWeight() {
        LocalDateTime birthdate = LocalDateTime.now().minusMonths(12);
        Animal animal = Animal.create(
                "Vis123", "Senasa123", "Key123", "Renspa123", "Int123",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.STEER,
                birthdate, 300.5, assignedLot
        );

        animal.updateWeight(350.0);
        assertEquals(350.0, animal.getCurrentWeight());
    }

    @Test
    void testMoveToLot() {
        LocalDateTime birthdate = LocalDateTime.now().minusMonths(12);
        Animal animal = Animal.create(
                "Vis123", "Senasa123", "Key123", "Renspa123", "Int123",
                Species.BOVINE, "Angus", Sex.MALE, CategoryAnimal.STEER,
                birthdate, 300.5, assignedLot
        );

        Polygon lotPolygon2 = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(2,2), new Coordinate(2,6), new Coordinate(6,6), new Coordinate(6,2), new Coordinate(2,2)});
        Lot newLot = Lot.create("Lote 2", 15.0, SoilType.SANDY, LotType.PASTURE, "Desc", lotPolygon2, assignedLot.getFarm());
        User user = User.create("Pepe", "Perez", "12345678", "555-1234", "Addr", "pepe@test.com", new Password("Password987!"));
        AnimalMovement movement = AnimalMovement.create(assignedLot, LocalDateTime.now().minusDays(1), user);
        animal.moveToLot(newLot, movement);

        assertEquals(newLot, animal.getAssignedLot());
        assertEquals(1, animal.getMovementHistory().size());
    }
}
