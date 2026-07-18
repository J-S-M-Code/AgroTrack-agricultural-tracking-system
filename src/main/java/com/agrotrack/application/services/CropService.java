package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Crop;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;
import com.agrotrack.domain.port.in.crop.RegisterCropUseCase;
import com.agrotrack.domain.port.in.crop.RegisterHarvestUseCase;
import com.agrotrack.domain.port.in.crop.UpdatePhenologicalStateUseCase;
import com.agrotrack.domain.port.out.crop.CropRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.domain.port.in.crop.GetCropByIdUseCase;
import com.agrotrack.domain.port.in.crop.GetCropsByFarmUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.agrotrack.domain.port.in.crop.UpdateCropUseCase;

@Service
public class CropService implements RegisterCropUseCase, UpdatePhenologicalStateUseCase, RegisterHarvestUseCase, GetCropsByFarmUseCase, GetCropByIdUseCase, UpdateCropUseCase {

    private final CropRepositoryPort cropRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort; // Necesario para validar dónde se planta

    public CropService(CropRepositoryPort cropRepositoryPort, LotRepositoryPort lotRepositoryPort) {
        this.cropRepositoryPort = cropRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
    }

    @Override
    @Transactional
    public Crop executeRegisterCrop(TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                        LocalDateTime estimateHarvestDate, UUID assignedLotId, double implantedSurface,
                        String renspa, PhenologicalState phenologicalState) {

        // 1. Validar que el lote donde se va a sembrar exista
        Lot lot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + assignedLotId));

        // Opcional: Podrías agregar una validación de negocio cruzada aquí si lo deseas,
        // por ejemplo, verificar que el lote esté en estado ACTIVO antes de sembrar.

        // 2. Instanciar la entidad a través de su factory method
        // (Las validaciones de negocio sobre la superficie y las fechas explotan adentro del create)
        Crop newCrop = Crop.create(
                typeCrop, species, variety, plantingDate, estimateHarvestDate,
                lot, implantedSurface, renspa, phenologicalState
        );

        // 3. Guardar en base de datos
        return cropRepositoryPort.save(newCrop);
    }

    @Override
    @Transactional
    public void executeUpdatePhenologicalState(UUID cropId, PhenologicalState newState) {

        Crop crop = cropRepositoryPort.findById(cropId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Cultivo no encontrado con ID: " + cropId));

        // El dominio se encarga de cambiar el estado
        crop.updatePhenologicalState(newState);

        cropRepositoryPort.save(crop);
    }

    @Override
    @Transactional
    public void executeRegisterHarvest(UUID cropId, LocalDateTime actualHarvestDate) {

        Crop crop = cropRepositoryPort.findById(cropId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Cultivo no encontrado con ID: " + cropId));

        // Validamos si efectivamente la fecha de la máquina cosechadora es coherente
        if (!crop.isReadyForHarvest(actualHarvestDate)) {
            // Nota: Dependiendo de tus reglas, puedes hacer que esto sea solo un Warning y dejarlo pasar,
            // pero si somos estrictos, evitamos que se registre una cosecha antes de tiempo.
            throw new BusinessRuleViolationsException("El cultivo aún no está en fecha para ser cosechado según la estimación.");
        }

        // Actualizamos el estado a COSECHADO (HARVESTED)
        crop.updatePhenologicalState(PhenologicalState.HARVESTED);

        // Si quieres llevar registro exacto de la fecha, usamos el método de la entidad
        // (aunque el setHarvestDate valida que no sea anterior a la fecha de plantación)
        crop.setHarvestDate(actualHarvestDate);

        cropRepositoryPort.save(crop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Crop> executeGetCropsByFarm(UUID farmId) {
        return cropRepositoryPort.findByFarmId(farmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Crop executeGetCropById(UUID cropId) {
        return cropRepositoryPort.findById(cropId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Cultivo no encontrado con ID: " + cropId));
    }

    @Override
    @Transactional
    public Crop executeUpdateCrop(UUID cropId, TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                           LocalDateTime estimateHarvestDate, UUID assignedLotId, double implantedSurface,
                           String renspa, PhenologicalState phenologicalState) {
                           
        Crop crop = cropRepositoryPort.findById(cropId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Cultivo no encontrado con ID: " + cropId));
                
        Lot lot = lotRepositoryPort.findById(assignedLotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + assignedLotId));

        crop.update(typeCrop, species, variety, plantingDate, estimateHarvestDate, lot, implantedSurface, renspa, phenologicalState);

        return cropRepositoryPort.save(crop);
    }
}