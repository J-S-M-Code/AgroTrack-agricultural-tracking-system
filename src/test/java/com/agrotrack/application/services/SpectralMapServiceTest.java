package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
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
    private FarmRepositoryPort farmRepositoryPort;

    @InjectMocks
    private SpectralMapService spectralMapService;

    private Farm farm;
    private UUID farmId;

    @BeforeEach
    void setUp() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        farmId = UUID.randomUUID();

        farm = mock(Farm.class);
        lenient().when(farm.getIdFarm()).thenReturn(farmId);
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.transaction.support.TransactionSynchronizationManager.clear();
    }

    @Test
    void executeRegisterSpectralMapSuccess() {
        String mockPath = "bucket/path/map.tif";
        LocalDateTime flightDate = LocalDateTime.now().minusDays(1);
        
        when(farmRepositoryPort.findById(farmId)).thenReturn(Optional.of(farm));
        when(spectralMapRepositoryPort.save(any(SpectralMap.class))).thenAnswer(i -> {
            SpectralMap map = i.getArgument(0);
            return map;
        });

        SpectralMap newMap = spectralMapService.executeRegisterSpectralMap(
                mockPath, flightDate, SpectralMapType.NDVI, 5.0, 10.0, 0.75, farmId, "Test description"
        );

        assertNotNull(newMap);
        assertNotNull(newMap.getUrlSpectralMap());
        assertEquals(mockPath, newMap.getUrlSpectralMap());
        assertEquals(SpectralMapType.NDVI, newMap.getIndexType());
        
        verify(spectralMapRepositoryPort, times(1)).save(any(SpectralMap.class));
    }

    @Test
    void executeRegisterSpectralMapThrowsExceptionWhenLotNotFound() {
        String mockPath = "bucket/path/map.tif";
        when(farmRepositoryPort.findById(farmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                spectralMapService.executeRegisterSpectralMap(
                        mockPath, LocalDateTime.now(), SpectralMapType.NDVI, 5.0, 10.0, 0.75, farmId, "Test desc"
                )
        );

        verify(spectralMapRepositoryPort, never()).save(any(SpectralMap.class));
    }
}
