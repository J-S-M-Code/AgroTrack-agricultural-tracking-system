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
import com.agrotrack.domain.port.out.storage.FileStoragePort;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlertService alertService;

    private User author;
    private Lot lot;
    private UUID authorId;
    private UUID lotId;
    private UUID farmId;

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        lotId = UUID.randomUUID();
        farmId = UUID.randomUUID();

        author = mock(User.class);
        lot = mock(Lot.class);
        
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        lenient().when(lot.getPolygonLimit()).thenReturn(lotPolygon);
    }

    @Test
    void executeCreateAlertSuccessWithGpsPointAndRelativeImages() {
        Point gpsPoint = geometryFactory.createPoint(new Coordinate(-67.49, -29.16));
        List<String> relativeImages = List.of("novedades/" + farmId + "/img1.jpg");

        when(userRepositoryPort.findById(authorId)).thenReturn(Optional.of(author));
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.of(lot));
        when(alertRepositoryPort.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert newAlert = alertService.execute(
                "Alerta 1", AlertType.MANUAL_NOTE, Priority.HIGH, RecordType.ALERT,
                "Desc", LocalDateTime.now(), authorId, relativeImages, lotId, null, null, null, gpsPoint
        );

        assertNotNull(newAlert);
        assertEquals("Alerta 1", newAlert.getTitle());
        assertEquals(relativeImages, newAlert.getImages());
        assertEquals(-67.49, newAlert.getCentroid().getX(), 1e-6);
        assertEquals(-29.16, newAlert.getCentroid().getY(), 1e-6);
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
        
        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.execute(
                        "Alerta 1", AlertType.SYSTEM_AUTOMATIC, Priority.HIGH, RecordType.ALERT,
                        "Desc", LocalDateTime.now(), authorId, new ArrayList<>(), null, null, null, null, null
                )
        );

        verify(alertRepositoryPort, never()).save(any(Alert.class));
    }

    @Test
    void executeUploadAlertImageJpegReturnsCleanRelativePathIgnoringInternalHostnameAndClientFilename() {
        byte[] validJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01};
        when(fileStoragePort.uploadFile(anyString(), any(InputStream.class), eq((long) validJpeg.length), eq("image/jpeg")))
                .thenReturn("http://azurite-azuretest:10000/devstoreaccount1/agrotrack-files/ignored-internal-url.jpg");

        String relativePath = alertService.executeUploadAlertImage(
                farmId,
                "../../etc/passwd/foto maliciosa.JPG",
                "image/jpeg",
                validJpeg.length,
                new ByteArrayInputStream(validJpeg)
        );

        assertNotNull(relativePath);
        assertFalse(relativePath.contains("http://"));
        assertFalse(relativePath.contains(".."));
        assertFalse(relativePath.contains("passwd"));
        assertTrue(relativePath.matches("^novedades/" + farmId + "/[0-9a-f-]{36}\\.jpg$"),
                "Unexpected relative path: " + relativePath);
        verify(fileStoragePort).uploadFile(eq(relativePath), any(InputStream.class), eq((long) validJpeg.length), eq("image/jpeg"));
    }

    @Test
    void executeUploadAlertImagePngAndWebpSuccess() {
        byte[] validPng = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
        byte[] validWebp = new byte[]{'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

        when(fileStoragePort.uploadFile(anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn("http://azurite-azuretest:10000/blob");

        String pngPath = alertService.executeUploadAlertImage(
                farmId, "captura.png", "image/png", validPng.length, new ByteArrayInputStream(validPng)
        );
        String webpPath = alertService.executeUploadAlertImage(
                farmId, "foto_celular.webp", "image/webp", validWebp.length, new ByteArrayInputStream(validWebp)
        );

        assertTrue(pngPath.matches("^novedades/" + farmId + "/[0-9a-f-]{36}\\.png$"));
        assertTrue(webpPath.matches("^novedades/" + farmId + "/[0-9a-f-]{36}\\.webp$"));
    }

    @Test
    void executeUploadAlertImageRejectsEmptyFile() {
        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.executeUploadAlertImage(farmId, "vacio.jpg", "image/jpeg", 0, new ByteArrayInputStream(new byte[0]))
        );
        verify(fileStoragePort, never()).uploadFile(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void executeUploadAlertImageRejectsDisallowedContentTypeOrExtension() {
        byte[] svgBytes = "<svg><script>alert(1)</script></svg>".getBytes(StandardCharsets.UTF_8);

        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.executeUploadAlertImage(farmId, "xss.svg", "image/svg+xml", svgBytes.length, new ByteArrayInputStream(svgBytes))
        );

        byte[] validJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.executeUploadAlertImage(farmId, "disfrazado.exe", "image/jpeg", validJpeg.length, new ByteArrayInputStream(validJpeg))
        );
        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.executeUploadAlertImage(farmId, "mismatch.png", "image/jpeg", validJpeg.length, new ByteArrayInputStream(validJpeg))
        );

        verify(fileStoragePort, never()).uploadFile(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void executeUploadAlertImageRejectsSpoofedMagicBytes() {
        byte[] fakeExecutable = "MZ-not-a-real-jpeg-binary".getBytes(StandardCharsets.UTF_8);

        assertThrows(BusinessRuleViolationsException.class, () ->
                alertService.executeUploadAlertImage(farmId, "payload.jpg", "image/jpeg", fakeExecutable.length, new ByteArrayInputStream(fakeExecutable))
        );

        verify(fileStoragePort, never()).uploadFile(anyString(), any(), anyLong(), anyString());
    }
}
