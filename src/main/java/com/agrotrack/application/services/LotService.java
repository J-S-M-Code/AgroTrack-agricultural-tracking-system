package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import com.agrotrack.domain.port.in.lot.ChangeLotStateUseCase;
import com.agrotrack.domain.port.in.lot.CreateLotUseCase;
import com.agrotrack.domain.port.in.lot.GetLotsByFarmUseCase;
import com.agrotrack.domain.port.in.lot.GetUnassignedLotsUseCase;
import com.agrotrack.domain.port.in.lot.RevokeLotUseCase;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.domain.port.out.lot.LotRepositoryPort;
import com.agrotrack.application.dto.LotDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import java.util.List;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.agrotrack.domain.port.in.lot.UpdateLotUseCase;

@Service
public class LotService implements CreateLotUseCase, ChangeLotStateUseCase, GetLotsByFarmUseCase, RevokeLotUseCase, GetUnassignedLotsUseCase, UpdateLotUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final FarmRepositoryPort farmRepositoryPort;
    private final ApplicationDtoMapper applicationDtoMapper;

    public LotService(LotRepositoryPort lotRepositoryPort, FarmRepositoryPort farmRepositoryPort, ApplicationDtoMapper applicationDtoMapper) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.farmRepositoryPort = farmRepositoryPort;
        this.applicationDtoMapper = applicationDtoMapper;
    }

    @Override
    @Transactional
    public Lot executeCreateLot(UUID farmId, String name, double hectares, SoilType soilType,
                       LotType type, String description, Polygon polygonLimit) {

        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + farmId));

        // 1. Validar que el Lote no se superponga con otro Lote existente
        if (lotRepositoryPort.existsOverlappingLot(polygonLimit, null, type)) {
            throw new BusinessRuleViolationsException("El perímetro ingresado se superpone con un lote ya existente.");
        }

        // 2. ¡Magia Geospacial! Validar que el lote esté físicamente DENTRO de la finca
        // Agregamos una pequeña tolerancia (aprox 5 metros = 0.00005 grados) para absorber errores de redondeo al hacer snap a los bordes
        double toleranceInDegrees = 0.00005;
        if (!farm.getPolygonLimit().buffer(toleranceInDegrees).contains(polygonLimit)) {
            throw new BusinessRuleViolationsException("El perímetro del lote debe estar completamente dentro de los límites de la finca.");
        }

        // 3. Instanciar la entidad a través de su factory method
        Lot newLot = Lot.create(
                name,
                hectares,
                soilType,
                type,
                description,
                polygonLimit,
                farm
        );

        // 4. Agregar el lote a la lista de la finca
        farm.newLot(newLot);

        // 5. Persistencia
        // Guardamos el lote. Si tu BD relacional requiere actualizar la finca para
        // mantener la relación FK, también guardamos la finca.
        Lot savedLot = lotRepositoryPort.save(newLot);
        farmRepositoryPort.save(farm);

        return savedLot;
    }

    @Override
    @Transactional
    public void executeChangeLotState(UUID lotId, LotState newState) {
        // Buscar, cambiar estado y guardar. Súper limpio.
        Lot lot = lotRepositoryPort.findById(lotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + lotId));

        lot.changeState(newState);

        lotRepositoryPort.save(lot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LotDto> executeGetLotsByFarm(UUID farmId) {
        List<Lot> lots = lotRepositoryPort.findByFarmId(farmId);
        return applicationDtoMapper.toLotDtoList(lots);
    }

    @Override
    public List<Lot> executeGetUnassignedLots(UUID userId) {
        return lotRepositoryPort.findUnassignedLotsByUserId(userId);
    }

    @Override
    @Transactional
    public void execute(UUID lotId, String reason) {
        Lot lot = lotRepositoryPort.findById(lotId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + lotId));

        if (lot.getState() == LotState.INACTIVE) {
            throw new BusinessRuleViolationsException("El lote ya se encuentra inactivo");
        }
        
        lot.setDeletionReason(reason);
        lot.changeState(LotState.INACTIVE);
        
        lotRepositoryPort.save(lot);
    }

    @Override
    @Transactional
    public Lot executeUpdateLot(UUID idLot, String name, double hectares, SoilType soilType,
                                LotType type, String description, Polygon polygonLimit) {
        Lot lot = lotRepositoryPort.findById(idLot)
                .orElseThrow(() -> new BusinessRuleViolationsException("Lote no encontrado con ID: " + idLot));

        // Validar que el nuevo polígono esté físicamente DENTRO de la finca
        Farm farm = lot.getFarm();
        double toleranceInDegrees = 0.00005;
        if (!farm.getPolygonLimit().buffer(toleranceInDegrees).contains(polygonLimit)) {
            throw new BusinessRuleViolationsException("El perímetro del lote debe estar completamente dentro de los límites de la finca.");
        }

        // Validar que no se superponga con otros lotes (excluyendo el lote actual)
        if (lotRepositoryPort.existsOverlappingLot(polygonLimit, idLot, type)) {
            throw new BusinessRuleViolationsException("El perímetro ingresado se superpone con un lote ya existente.");
        }

        lot.update(name, hectares, soilType, type, description, polygonLimit);

        return lotRepositoryPort.save(lot);
    }
}