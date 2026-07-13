package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Animal;
import com.agrotrack.domain.model.entities.IoTCollar;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.State;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.iot.IoTCollarRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IoTCollarServiceTest {

    private IoTCollarRepositoryPort ioTCollarRepositoryPort;
    private AnimalRepositoryPort animalRepositoryPort;
    private IoTCollarService ioTCollarService;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        ioTCollarRepositoryPort = mock(IoTCollarRepositoryPort.class);
        animalRepositoryPort = mock(AnimalRepositoryPort.class);
        ioTCollarService = new IoTCollarService(ioTCollarRepositoryPort, animalRepositoryPort);
        geometryFactory = new GeometryFactory();
    }

    @Test
    void executeRegisterIoTCollar_ShouldSaveAndReturnCollar() {
        // Arrange
        when(ioTCollarRepositoryPort.findByCodeRFID("RFID-123")).thenReturn(Optional.empty());
        when(ioTCollarRepositoryPort.save(any(IoTCollar.class))).thenAnswer(invocation -> {
            IoTCollar collar = invocation.getArgument(0);
            collar.setIdCollar(UUID.randomUUID());
            return collar;
        });

        // Act
        IoTCollar result = ioTCollarService.executeRegisterIoTCollar("RFID-123", "Model A", State.AVAILABLE, 100.0, null);

        // Assert
        assertNotNull(result);
        assertEquals("RFID-123", result.getCodeRFID());
        verify(ioTCollarRepositoryPort, times(1)).save(any(IoTCollar.class));
    }

    @Test
    void executeRegisterIoTCollar_WhenRfidExists_ShouldThrowException() {
        // Arrange
        when(ioTCollarRepositoryPort.findByCodeRFID("RFID-123")).thenReturn(Optional.of(IoTCollar.create("RFID-123", "V1", State.AVAILABLE, 100.0, null)));

        // Act & Assert
        assertThrows(BusinessRuleViolationsException.class, () -> 
            ioTCollarService.executeRegisterIoTCollar("RFID-123", "Model A", State.AVAILABLE, 100.0, null)
        );
    }

    @Test
    void executeRegisterGPSPosition_ShouldAddPositionAndSave() {
        // Arrange
        UUID collarId = UUID.randomUUID();
        IoTCollar collar = IoTCollar.create("RFID-123", "Model A", State.AVAILABLE, 100.0, null);
        collar.setIdCollar(collarId);

        when(ioTCollarRepositoryPort.findById(collarId)).thenReturn(Optional.of(collar));
        when(animalRepositoryPort.findByCollarId(collarId)).thenReturn(Optional.empty());

        Point point = geometryFactory.createPoint(new Coordinate(10, 10));

        // Act
        ioTCollarService.executeRegisterGPSPosition(collarId, LocalDateTime.now(), point);

        // Assert
        assertEquals(1, collar.getGpsHistory().size());
        verify(ioTCollarRepositoryPort, times(1)).save(collar);
    }
}
