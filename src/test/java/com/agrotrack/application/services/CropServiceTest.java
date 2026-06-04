package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
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
class CropServiceTest {

    @Mock
    private CropRepositoryPort cropRepositoryPort;

    @Mock
    private LotRepositoryPort lotRepositoryPort;

    @InjectMocks
    private CropService cropService;

    private Lot lot;
    private Crop crop;
    private UUID lotId;
    private UUID cropId;

    @BeforeEach
    void setUp() {
        lotId = UUID.randomUUID();
        cropId = UUID.randomUUID();

        lot = mock(Lot.class);
        lenient().when(lot.getHectares()).thenReturn(100.0);
        
        crop = mock(Crop.class);
    }

    @Test
    void executeRegisterCropSuccess() {
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.of(lot));
        when(cropRepositoryPort.save(any(Crop.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime plantingDate = LocalDateTime.now().minusDays(10);
        LocalDateTime estimateHarvest = LocalDateTime.now().plusDays(90);

        Crop newCrop = cropService.executeRegisterCrop(
                TypeCrop.CEREAL, "Trigo", "Bandeira", plantingDate, estimateHarvest,
                lotId, 50.0, "RENSPA-123", PhenologicalState.GERMINATION
        );

        assertNotNull(newCrop);
        assertEquals("Trigo", newCrop.getSpecies());
        verify(lotRepositoryPort).findById(lotId);
        verify(cropRepositoryPort).save(any(Crop.class));
    }

    @Test
    void executeRegisterCropThrowsExceptionWhenLotNotFound() {
        when(lotRepositoryPort.findById(lotId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationsException.class, () ->
                cropService.executeRegisterCrop(
                        TypeCrop.CEREAL, "Trigo", "Bandeira", LocalDateTime.now(), LocalDateTime.now().plusDays(90),
                        lotId, 50.0, "RENSPA-123", PhenologicalState.GERMINATION
                )
        );

        verify(cropRepositoryPort, never()).save(any(Crop.class));
    }

    @Test
    void executeUpdatePhenologicalStateSuccess() {
        when(cropRepositoryPort.findById(cropId)).thenReturn(Optional.of(crop));

        cropService.executeUpdatePhenologicalState(cropId, PhenologicalState.FLOWERING);

        verify(crop).updatePhenologicalState(PhenologicalState.FLOWERING);
        verify(cropRepositoryPort).save(crop);
    }

    @Test
    void executeUpdatePhenologicalStateThrowsExceptionWhenCropNotFound() {
        when(cropRepositoryPort.findById(cropId)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleViolationsException.class, () ->
                cropService.executeUpdatePhenologicalState(cropId, PhenologicalState.FLOWERING)
        );

        verify(cropRepositoryPort, never()).save(any(Crop.class));
    }

    @Test
    void executeRegisterHarvestSuccess() {
        LocalDateTime actualHarvest = LocalDateTime.now().plusDays(100);
        when(cropRepositoryPort.findById(cropId)).thenReturn(Optional.of(crop));
        when(crop.isReadyForHarvest(actualHarvest)).thenReturn(true);

        cropService.executeRegisterHarvest(cropId, actualHarvest);

        verify(crop).updatePhenologicalState(PhenologicalState.HARVESTED);
        verify(crop).setHarvestDate(actualHarvest);
        verify(cropRepositoryPort).save(crop);
    }

    @Test
    void executeRegisterHarvestThrowsExceptionWhenNotReady() {
        LocalDateTime actualHarvest = LocalDateTime.now().plusDays(10);
        when(cropRepositoryPort.findById(cropId)).thenReturn(Optional.of(crop));
        when(crop.isReadyForHarvest(actualHarvest)).thenReturn(false);

        assertThrows(BusinessRuleViolationsException.class, () ->
                cropService.executeRegisterHarvest(cropId, actualHarvest)
        );

        verify(crop, never()).updatePhenologicalState(any(PhenologicalState.class));
        verify(cropRepositoryPort, never()).save(any(Crop.class));
    }
}
