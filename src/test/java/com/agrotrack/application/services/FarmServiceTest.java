package com.agrotrack.application.services;

import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepositoryPort farmRepositoryPort;

    @Mock
    private ApplicationDtoMapper applicationDtoMapper;

    @InjectMocks
    private FarmService farmService;

    private GeometryFactory geometryFactory;
    private Polygon samplePolygon;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();
        samplePolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
        });
    }

    @Test
    void executeCreateFarm_Success() {
        // Arrange
        when(farmRepositoryPort.existsByCuit("30-11111111-9")).thenReturn(false);
        when(farmRepositoryPort.existsOverlappingFarm(samplePolygon, null)).thenReturn(false);
        when(farmRepositoryPort.save(any(Farm.class))).thenAnswer(invocation -> {
            Farm farm = invocation.getArgument(0);
            farm.setIdFarm(UUID.randomUUID());
            return farm;
        });

        // Act
        Farm result = farmService.executeCreateFarm(
                "Mi Finca", "Empresa", "30-11111111-9", "REN",
                ProductiveOrientation.AGRICULTURAL, "Dir", samplePolygon, 100.0, "url"
        );

        // Assert
        assertNotNull(result.getIdFarm());
        assertEquals("Mi Finca", result.getName());
        verify(farmRepositoryPort).save(any(Farm.class));
    }

    @Test
    void executeCreateFarm_ThrowsExceptionWhenCuitExists() {
        // Arrange
        when(farmRepositoryPort.existsByCuit("30-11111111-9")).thenReturn(true);

        // Act & Assert
        BusinessRuleViolationsException exception = assertThrows(
                BusinessRuleViolationsException.class,
                () -> farmService.executeCreateFarm(
                        "Mi Finca", "Empresa", "30-11111111-9", "REN",
                        ProductiveOrientation.AGRICULTURAL, "Dir", samplePolygon, 100.0, "url"
                )
        );
        assertTrue(exception.getMessage().contains("CUIT"));
        verify(farmRepositoryPort, never()).save(any(Farm.class));
    }

    @Test
    void executeGetFarmsByUser_ReturnsDtoList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Farm farm = Farm.create("A", "B", "C", "D", ProductiveOrientation.AGRICULTURAL, "E", samplePolygon, samplePolygon.getCentroid(), 1.0, "url");
        FarmDto dto = FarmDto.builder().name("A").build();

        when(farmRepositoryPort.findByUserId(userId)).thenReturn(List.of(farm));
        when(applicationDtoMapper.toFarmDtoList(List.of(farm))).thenReturn(List.of(dto));

        // Act
        List<FarmDto> result = farmService.executeGetFarmsByUser(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }
}
