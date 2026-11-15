package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.model.enums.SpectralMapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpectralMapTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Lot assignedLot;

    @BeforeEach
    void setUp() {
        Polygon farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        Farm farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        assignedLot = Lot.create("Lote 1", 10.5, SoilType.CLAYEY, LotType.AGRICULTURAL, "Desc", lotPolygon, farm);
    }

    @Test
    void testCreateSpectralMapSuccess() {
        LocalDateTime flightDate = LocalDateTime.now().minusDays(2);
        UUID farmId = UUID.randomUUID();
        
        SpectralMap map = SpectralMap.create(
                "http://url.com/map.tif",
                flightDate,
                SpectralMapType.NDVI,
                15.0, // 15% cloud cover
                1.5,  // 1.5 cm/px resolution
                0.75, // mean index
                farmId,
                "Test description"
        );
        
        assertNotNull(map);
        assertEquals(SpectralMapType.NDVI, map.getIndexType());
        assertEquals(15.0, map.getCloudCoverPercentage());
        assertEquals(farmId, map.getFarmId());
    }

    @Test
    void testIsReliable() {
        LocalDateTime flightDate = LocalDateTime.now().minusDays(2);
        UUID farmId = UUID.randomUUID();
        
        SpectralMap reliableMap = SpectralMap.create(
                "http://url.com/map.tif", flightDate, SpectralMapType.NDVI, 10.0, 1.5, 0.75, farmId, "Desc"
        );
        assertTrue(reliableMap.isReliable());
        
        SpectralMap unreliableMap = SpectralMap.create(
                "http://url.com/map.tif", flightDate, SpectralMapType.NDVI, 30.0, 1.5, 0.75, farmId, "Desc"
        );
        assertFalse(unreliableMap.isReliable());
    }

    @Test
    void testRequiresAttention() {
        LocalDateTime flightDate = LocalDateTime.now().minusDays(2);
        UUID farmId = UUID.randomUUID();
        
        SpectralMap map = SpectralMap.create(
                "http://url.com/map.tif", flightDate, SpectralMapType.NDVI, 10.0, 1.5, 0.50, farmId, "Desc"
        );
        
        assertTrue(map.requiresAttention(0.60)); // Threshold 0.60
        assertFalse(map.requiresAttention(0.40)); // Threshold 0.40
    }

    @Test
    void testIsRecent() {
        UUID farmId = UUID.randomUUID();
        SpectralMap recentMap = SpectralMap.create(
                "http://url.com/map.tif", LocalDateTime.now().minusDays(2), SpectralMapType.NDVI, 10.0, 1.5, 0.50, farmId, "Desc"
        );
        assertTrue(recentMap.isRecent());

        SpectralMap oldMap = SpectralMap.create(
                "http://url.com/map.tif", LocalDateTime.now().minusDays(10), SpectralMapType.NDVI, 10.0, 1.5, 0.50, farmId, "Desc"
        );
        assertFalse(oldMap.isRecent());
    }
}
