package com.agrotrack.application.services;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.domain.port.in.farm.CreateFarmUseCase;
import com.agrotrack.domain.port.in.farm.UpdateFarmPerimeterUseCase;
import com.agrotrack.domain.port.in.farm.GetFarmsUseCase;
import com.agrotrack.domain.port.in.farm.GetFarmByIdUseCase;
import com.agrotrack.domain.port.in.farm.UpdateFarmUseCase;
import com.agrotrack.domain.port.out.farm.FarmRepositoryPort;
import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.application.mapper.ApplicationDtoMapper;
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FarmService implements CreateFarmUseCase, UpdateFarmPerimeterUseCase, GetFarmsUseCase, GetFarmByIdUseCase, UpdateFarmUseCase {

    private final FarmRepositoryPort farmRepositoryPort;
    private final ApplicationDtoMapper applicationDtoMapper;

    public FarmService(FarmRepositoryPort farmRepositoryPort, ApplicationDtoMapper applicationDtoMapper) {
        this.farmRepositoryPort = farmRepositoryPort;
        this.applicationDtoMapper = applicationDtoMapper;
    }

    @Override
    @Transactional
    public Farm executeCreateFarm(String name, String companyName, String cuit, String numberRENAPSA,
                        ProductiveOrientation productiveOrientation, String address,
                        Polygon polygonLimit, double surface, String imageUrl) {

        if (farmRepositoryPort.existsByCuit(cuit)) {
            throw new BusinessRuleViolationsException("Ya existe una finca registrada con el CUIT: " + cuit);
        }

        if (polygonLimit != null && farmRepositoryPort.existsOverlappingFarm(polygonLimit, null)) {
            throw new BusinessRuleViolationsException("El perímetro ingresado se superpone con una finca existente en el sistema.");
        }

        // Calculamos el centroide de forma segura o lo mandamos nulo para
        // que la propia entidad lo calcule (como armamos en su validación)
        Point centroid = (polygonLimit != null) ? polygonLimit.getCentroid() : null;

        Farm newFarm = Farm.create(
                name,
                companyName,
                cuit,
                numberRENAPSA,
                productiveOrientation,
                address,
                polygonLimit,
                centroid,
                surface,
                imageUrl
        );

        return farmRepositoryPort.save(newFarm);
    }

    @Override
    @Transactional
    public void executeUpdateFarmPerimeter(UUID farmId, Polygon newPerimeter) {
        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + farmId));

        if (farmRepositoryPort.existsOverlappingFarm(newPerimeter, farmId)) {
            throw new BusinessRuleViolationsException("El nuevo perímetro se superpone con otra finca existente.");
        }

        farm.modifyPolygonLimit(newPerimeter);
        farmRepositoryPort.save(farm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmDto> executeGetFarmsByUser(UUID userId) {
        List<Farm> farms = farmRepositoryPort.findByUserId(userId);
        return applicationDtoMapper.toFarmDtoList(farms);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmDto executeGetFarmById(UUID farmId) {
        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + farmId));
        return applicationDtoMapper.toFarmDto(farm);
    }

    @Override
    @Transactional
    public Farm executeUpdateFarm(UUID farmId, String name, String companyName, String cuit, String numberRENAPSA,
                                  ProductiveOrientation productiveOrientation, String address, String imageUrl) {
        Farm farm = farmRepositoryPort.findById(farmId)
                .orElseThrow(() -> new BusinessRuleViolationsException("Finca no encontrada con ID: " + farmId));

        if (!farm.getCuit().equals(cuit) && farmRepositoryPort.existsByCuit(cuit)) {
            throw new BusinessRuleViolationsException("Ya existe otra finca registrada con el CUIT: " + cuit);
        }

        farm.update(name, companyName, cuit, numberRENAPSA, productiveOrientation, address, imageUrl);

        return farmRepositoryPort.save(farm);
    }
}