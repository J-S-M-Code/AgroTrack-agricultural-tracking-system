package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.State;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IoTCollarTest {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void testCreateIoTCollarSuccess() {
        IoTCollar collar = IoTCollar.create("RFID-1234", "ModelX", State.AVAILABLE, 100.0);
        
        assertNotNull(collar);
        assertEquals("RFID-1234", collar.getCodeRFID());
        assertEquals("ModelX", collar.getModel());
        assertEquals(State.AVAILABLE, collar.getState());
        assertEquals(100.0, collar.getBatteryLevel());
        assertTrue(collar.getGpsHistory().isEmpty());
    }

    @Test
    void testCreateIoTCollarThrowsExceptionWhenRFIDIsBlank() {
        assertThrows(BusinessRuleViolationsException.class, () -> 
            IoTCollar.create("", "ModelX", State.AVAILABLE, 100.0)
        );
    }

    @Test
    void testAddGpsPosition() {
        IoTCollar collar = IoTCollar.create("RFID-1234", "ModelX", State.AVAILABLE, 100.0);
        Point point = geometryFactory.createPoint(new Coordinate(0, 0));
        GPSPosition pos = GPSPosition.create(LocalDateTime.now(), point, false);
        
        collar.addGpsPosition(pos);
        
        assertEquals(1, collar.getGpsHistory().size());
        assertEquals(pos, collar.getGpsHistory().get(0));
    }

    @Test
    void testUpdateBatteryLevel() {
        IoTCollar collar = IoTCollar.create("RFID-1234", "ModelX", State.AVAILABLE, 100.0);
        
        collar.updateBatteryLevel(50.0);
        assertEquals(50.0, collar.getBatteryLevel());
        assertFalse(collar.isLowBattery());
        
        collar.updateBatteryLevel(10.0);
        assertTrue(collar.isLowBattery());
    }

    @Test
    void testChangeState() {
        IoTCollar collar = IoTCollar.create("RFID-1234", "ModelX", State.AVAILABLE, 100.0);
        collar.changeState(State.MAINTENANCE);
        assertEquals(State.MAINTENANCE, collar.getState());
    }
}
