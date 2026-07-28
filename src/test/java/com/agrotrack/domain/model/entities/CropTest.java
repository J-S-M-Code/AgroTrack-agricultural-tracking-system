package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.model.enums.TypeCrop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CropTest {

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
    void testCreateCropSuccess() {
        LocalDateTime plantingDate = LocalDateTime.now().minusDays(10);
        LocalDateTime estimateHarvestDate = LocalDateTime.now().plusMonths(3);

        Crop crop = Crop.create(
                TypeCrop.CEREAL, "Trigo", "Baguette", plantingDate,
                estimateHarvestDate, assignedLot, 5.0, "Renspa1", PhenologicalState.GERMINATION
        );

        assertNotNull(crop);
        assertEquals(TypeCrop.CEREAL, crop.getTypeCrop());
        assertEquals("Trigo", crop.getSpecies());
        assertEquals(5.0, crop.getImplantedSurface());
        assertEquals(PhenologicalState.GERMINATION, crop.getPhenologicalState());
    }

    @Test
    void testCreateCropThrowsExceptionWhenImplantedSurfaceExceedsLot() {
        LocalDateTime plantingDate = LocalDateTime.now().minusDays(10);
        LocalDateTime estimateHarvestDate = LocalDateTime.now().plusMonths(3);

        // assignedLot has 10.5 hectares. Trying to plant 15.0
        assertThrows(BusinessRuleViolationsException.class, () -> 
            Crop.create(
                TypeCrop.CEREAL, "Trigo", "Baguette", plantingDate,
                estimateHarvestDate, assignedLot, 15.0, "Renspa1", PhenologicalState.GERMINATION
            )
        );
    }

    @Test
    void testSetHarvestDate() {
        LocalDateTime plantingDate = LocalDateTime.now().minusDays(10);
        LocalDateTime estimateHarvestDate = LocalDateTime.now().plusMonths(3);

        Crop crop = Crop.create(
                TypeCrop.CEREAL, "Trigo", "Baguette", plantingDate,
                estimateHarvestDate, assignedLot, 5.0, "Renspa1", PhenologicalState.GERMINATION
        );

        LocalDateTime newHarvestDate = estimateHarvestDate.plusDays(15);
        crop.setHarvestDate(newHarvestDate);

        assertEquals(newHarvestDate, crop.getEstimateHarvestDate());
    }

    @Test
    void testIsReadyForHarvest() {
        LocalDateTime plantingDate = LocalDateTime.now().minusDays(100);
        LocalDateTime estimateHarvestDate = LocalDateTime.now().minusDays(5); // Harvest date already passed

        Crop crop = Crop.create(
                TypeCrop.CEREAL, "Trigo", "Baguette", plantingDate,
                estimateHarvestDate, assignedLot, 5.0, "Renspa1", PhenologicalState.GERMINATION
        );

        assertTrue(crop.isReadyForHarvest(LocalDateTime.now()));
    }
}
