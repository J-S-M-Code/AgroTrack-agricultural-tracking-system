package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GPSPositionTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testCreateGPSPositionSuccess() {
        Point point = geometryFactory.createPoint(new Coordinate(10, 20));
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(5);
        
        GPSPosition pos = GPSPosition.create(timestamp, point, false);
        
        assertNotNull(pos);
        assertEquals(timestamp, pos.getTimestamp());
        assertEquals(point, pos.getCoordinate());
        assertFalse(pos.isOutOfBounds());
    }

    @Test
    void testCreateGPSPositionThrowsExceptionWhenFutureDate() {
        Point point = geometryFactory.createPoint(new Coordinate(10, 20));
        
        assertThrows(BusinessRuleViolationsException.class, () -> 
            GPSPosition.create(LocalDateTime.now().plusDays(1), point, false)
        );
    }

    @Test
    void testCreateGPSPositionThrowsExceptionWhenPointIsNull() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            GPSPosition.create(LocalDateTime.now(), null, false)
        );
    }

    @Test
    void testMarkAsOutOfBounds() {
        Point point = geometryFactory.createPoint(new Coordinate(10, 20));
        GPSPosition pos = GPSPosition.create(LocalDateTime.now().minusMinutes(5), point, false);
        
        pos.markAsOutOfBounds();
        assertTrue(pos.isOutOfBounds());
    }

    @Test
    void testMarkAsWithinBounds() {
        Point point = geometryFactory.createPoint(new Coordinate(10, 20));
        GPSPosition pos = GPSPosition.create(LocalDateTime.now().minusMinutes(5), point, true);
        
        pos.markAsWithinBounds();
        assertFalse(pos.isOutOfBounds());
    }
}
