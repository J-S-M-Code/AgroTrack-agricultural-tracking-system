package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Alert;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.AlertType;
import com.agrotrack.domain.model.enums.Priority;
import com.agrotrack.domain.model.enums.RecordType;
import com.agrotrack.domain.port.out.alert.AlertRepositoryPort;
import com.agrotrack.domain.port.out.animal.AnimalRepositoryPort;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepositoryPort alertRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private LotRepositoryPort lotRepositoryPort;

    @Mock
    private CropRepositoryPort cropRepositoryPort;

    @Mock
    private AnimalRepositoryPort animalRepositoryPort;

    @InjectMocks
    private AlertService alertService;

    private User author;
    private Lot lot;
    private UUID authorId;
    private UUID lotId;

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        lotId = UUID.randomUUID();

        author = mock(User.class);
        lot = mock(Lot.class);
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        lenient().when(lot.getPolygonLimit()).thenReturn(lotPolygon);
    }

    @Test
    void executeCreateAlertSuccess() {
        when(userRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.of(lot));
        when(alertRepositoryPort.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert newAlert = alertService.execute(
                "Alerta 1", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                "Desc", LocalDateTime.now(), authorId, new ArrayList<>(), lotId, null, null, null, null
        );

        assertNotNull(newAlert);
        assertEquals("Alerta 1", newAlert.getTitle());
        verify(userRepositoryPort).findById(authorId);
        verify(lotRepositoryPort).findById(lotId);
        verify(alertRepositoryPort).save(any(Alert.class));
    }

    @Test
    void executeCreateAlertThrowsExceptionWhenAuthorNotFound() {
        when(userRepositoryPort.findById(authorId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.execute(
                        "Alerta 1", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                        "Desc", LocalDateTime.now(), authorId, new ArrayList<>(), lotId, null, null, null, null
                )
        );

        verify(alertRepositoryPort, never()).save(any(Alert.class));
    }

    @Test
    void executeCreateAlertThrowsExceptionWhenNoRelatedEntity() {
        when(userRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));
        
        // No pasamos ni lote, ni cultivo, ni animal ni polígono. El dominio lo rechazará.
        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.execute(
                        "Alerta 1", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                        "Desc", LocalDateTime.now(), authorId, new ArrayList<>(), null, null, null, null, null
                )
        );

        verify(alertRepositoryPort, never()).save(any(Alert.class));
    }
}
