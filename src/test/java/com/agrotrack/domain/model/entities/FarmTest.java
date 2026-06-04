package com.agrotrack.domain.model.entities;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;
import com.agrotrack.domain.model.enums.ProductiveOrientation;

import static org.junit.jupiter.api.Assertions.*;

class FarmTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void testCreateFarm() {
        // Arrange
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
        });
        Point centroid = polygon.getCentroid();

        // Act
        Farm farm = Farm.create(
                "Mi Finca",
                "Empresa SA",
                "30-12345678-9",
                "RENAPSA-123",
                ProductiveOrientation.AGRICULTURAL,
                "Ruta 1 Km 10",
                polygon,
                centroid,
                100.0,
                "http://image.com/finca.jpg"
        );

        // Assert
        assertNotNull(farm);
        assertNull(farm.getIdFarm(), "El ID debe ser null al crear, ya que se asigna en persistencia");
        assertEquals("Mi Finca", farm.getName());
        assertEquals("30-12345678-9", farm.getCuit());
        assertEquals(polygon, farm.getPolygonLimit());
        assertEquals(centroid, farm.getCentroid());
        assertEquals(100.0, farm.getSurface());
    }

    @Test
    void testChangeId() {
        // Arrange
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,1), new Coordinate(1,1), new Coordinate(1,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 10.0, "url");
        UUID newId = UUID.randomUUID();

        // Act
        farm.setIdFarm(newId);

        // Assert
        assertEquals(newId, farm.getIdFarm());
    }
}
