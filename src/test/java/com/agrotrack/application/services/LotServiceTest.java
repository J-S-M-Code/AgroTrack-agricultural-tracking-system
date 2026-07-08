package com.agrotrack.application.services;

import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepositoryPort lotRepositoryPort;

    @Mock
    private FarmRepositoryPort farmRepositoryPort;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @InjectMocks
    private LotService lotService;

    private GeometryFactory geometryFactory = new GeometryFactory();
    private Farm farm;
    private Polygon farmPolygon;

    @BeforeEach
    void setUp() {
        farmPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(0,0)});
        farm = Farm.create("Finca", "Empresa", "CUIT", "RENAPSA", com.agrotrack.domain.model.enums.ProductiveOrientation.AGRICULTURAL, "Address", farmPolygon, farmPolygon.getCentroid(), 100.0, "url");
    }

    @Test
    void executeCreateLot_Success() {
        // Arrange
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        UUID farmId = UUID.randomUUID();
        when(farmRepositoryPort.findById(farmId)).thenReturn(java.util.Optional.of(farm));
        when(lotRepositoryPort.existsOverlappingLot(lotPolygon, null)).thenReturn(false);
        when(lotRepositoryPort.save(any(Lot.class))).thenAnswer(i -> {
            Lot l = i.getArgument(0);
            l.setIdLot(UUID.randomUUID());
            return l;
        });

        // Act
        Lot result = lotService.executeCreateLot(farmId, "Lote 1", 10.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", lotPolygon);

        // Assert
        assertNotNull(result.getIdLot());
        assertEquals("Lote 1", result.getName());
        verify(lotRepositoryPort, times(1)).save(any(Lot.class));
        verify(farmRepositoryPort, times(1)).save(farm);
    }

    @Test
    void executeCreateLot_ThrowsExceptionWhenOverlaps() {
        // Arrange
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(1,1), new Coordinate(1,5), new Coordinate(5,5), new Coordinate(5,1), new Coordinate(1,1)});
        UUID farmId = UUID.randomUUID();
        when(farmRepositoryPort.findById(farmId)).thenReturn(java.util.Optional.of(farm));
        when(lotRepositoryPort.existsOverlappingLot(lotPolygon, null)).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleViolationsException.class, () -> 
            lotService.executeCreateLot(farmId, "Lote 1", 10.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", lotPolygon)
        );
    }

    @Test
    void executeCreateLot_ThrowsExceptionWhenOutsideFarm() {
        // Arrange (polygon outside farm)
        Polygon lotPolygon = geometryFactory.createPolygon(new Coordinate[]{new Coordinate(20,20), new Coordinate(20,25), new Coordinate(25,25), new Coordinate(25,20), new Coordinate(20,20)});
        UUID farmId = UUID.randomUUID();
        when(farmRepositoryPort.findById(farmId)).thenReturn(java.util.Optional.of(farm));
        when(lotRepositoryPort.existsOverlappingLot(lotPolygon, null)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(BusinessRuleViolationsException.class, () -> 
            lotService.executeCreateLot(farmId, "Lote 1", 10.0, SoilType.CLAYEY, LotType.AGREICULTURAL, "Desc", lotPolygon)
        );
        assertTrue(exception.getMessage().contains("fuera") || exception.getMessage().contains("dentro"));
    }
}
