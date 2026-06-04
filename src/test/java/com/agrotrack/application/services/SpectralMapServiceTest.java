package com.agrotrack.application.services;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.crop.SpectralMapRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
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
    private FileStoragePort fileStoragePort;

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
        InputStream mockStream = mock(InputStream.class);
        LocalDateTime flightDate = LocalDateTime.now().minusDays(1);
        
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.of(lot));
        when(spectralMapRepositoryPort.save(any(SpectralMap.class))).thenAnswer(i -> {
            SpectralMap map = i.getArgument(0);
            return map;
        });

        SpectralMap newMap = spectralMapService.executeRegisterSpectralMap(
                mockStream, flightDate, SpectralMapType.NDVI, 5.0, 10.0, 0.75, lotId
        );

        assertNotNull(newMap);
        assertNotNull(newMap.getUrlSpectralMap());
        assertEquals(SpectralMapType.NDVI, newMap.getIndexType());
        
        verify(fileStoragePort).uploadFile(anyString(), eq(mockStream), eq("image/tiff"));
        verify(asyncMapProcessor).processAndTileMap(eq(newMap), anyString(), eq(SpectralMapType.NDVI));
        verify(spectralMapRepositoryPort, times(2)).save(any(SpectralMap.class));
    }

    @Test
    void executeRegisterSpectralMapThrowsExceptionWhenLotNotFound() {
        InputStream mockStream = mock(InputStream.class);
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                spectralMapService.executeRegisterSpectralMap(
                        mockStream, LocalDateTime.now(), SpectralMapType.NDVI, 5.0, 10.0, 0.75, lotId
                )
        );

        verify(spectralMapRepositoryPort, never()).save(any(SpectralMap.class));
        verify(fileStoragePort, never()).uploadFile(anyString(), any(InputStream.class), anyString());
    }
}
