package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpectralMapServiceTest {

    @Mock
    private SpectralMapRepositoryPort spectralMapRepositoryPort;

    @Mock
    private AsyncMapProcessor asyncMapProcessor;

    @Mock
    private LotRepositoryPort lotRepositoryPort;

    @InjectMocks
    private SpectralMapService spectralMapService;

    private Lot lot;
    private Farm farm;
    private UUID lotId;

    @BeforeEach
    void setUp() {
        lotId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();

        farm = mock(Farm.class);
        lenient().when(farm.getIdFarm()).thenReturn(farmId);

        lot = mock(Lot.class);
        lenient().when(lot.getIdLot()).thenReturn(lotId);
        lenient().when(lot.getFarm()).thenReturn(farm);
    }

    @Test
    void executeRegisterSpectralMapSuccess() {
        String mockPath = "bucket/path/map.tif";
        LocalDateTime flightDate = LocalDateTime.now().minusDays(1);
        
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.of(lot));
        when(spectralMapRepositoryPort.save(any(SpectralMap.class))).thenAnswer(i -> {
            SpectralMap map = i.getArgument(0);
            return map;
        });

        SpectralMap newMap = spectralMapService.executeRegisterSpectralMap(
                mockPath, flightDate, SpectralMapType.NDVI, 5.0, 10.0, 0.75, lotId
        );

        assertNotNull(newMap);
        assertNotNull(newMap.getUrlSpectralMap());
        assertEquals(mockPath, newMap.getUrlSpectralMap());
        assertEquals(SpectralMapType.NDVI, newMap.getIndexType());
        
        verify(asyncMapProcessor).processAndTileMap(eq(newMap), eq(mockPath), eq(SpectralMapType.NDVI));
        verify(spectralMapRepositoryPort, times(1)).save(any(SpectralMap.class));
    }

    @Test
    void executeRegisterSpectralMapThrowsExceptionWhenLotNotFound() {
        String mockPath = "bucket/path/map.tif";
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                spectralMapService.executeRegisterSpectralMap(
                        mockPath, LocalDateTime.now(), SpectralMapType.NDVI, 5.0, 10.0, 0.75, lotId
                )
        );

        verify(spectralMapRepositoryPort, never()).save(any(SpectralMap.class));
    }
}
