package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.*;

class LotTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void testCreateLotSuccess() {
        // Arrange
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});

        // Act
        Lot lot = Lot.create(
                "Lote 1", 
                10.5, 
                SoilType.CLAYEY, 
                LotType.AGRICULTURAL, 
                "Lote de prueba", 
                lotPolygon, 
                farm
        );

        // Assert
        assertNotNull(lot);
        assertEquals("Lote 1", lot.getName());
        assertEquals(10.5, lot.getHectares());
        assertEquals(SoilType.CLAYEY, lot.getSoilType());
        assertEquals(LotType.AGRICULTURAL, lot.getType());
        assertEquals(LotState.ACTIVE, lot.getState());
        assertEquals(farm, lot.getFarm());
        assertNotNull(lot.getAnimals());
        assertNotNull(lot.getSpectralMaps());
    }

    @Test
    void testCreateLotThrowsExceptionWhenNameIsBlank() {
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,1), new Coordinate(1,1), new Coordinate(1,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 100.0, "url");
        
        Exception exception = assertThrows(BusinessRuleViolationsException.class, () -> 
            Lot.create("", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", polygon, farm)
        );
        assertTrue(exception.getMessage().contains("El campo Nombre no puede estar vacío"));
    }

    @Test
    void testChangeState() {
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,1), new Coordinate(1,1), new Coordinate(1,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", polygon, polygon.getCentroid(), 100.0, "url");
        Lot lot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", polygon, farm);
        
        lot.changeState(LotState.INACTIVE);
        assertEquals(LotState.INACTIVE, lot.getState());
    }
}
