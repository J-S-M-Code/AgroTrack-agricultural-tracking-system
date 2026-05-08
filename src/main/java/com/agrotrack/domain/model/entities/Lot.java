package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Lot {
    @Getter
    @Setter
    private UUID idField;

    @Getter
    private String name;

    @Getter
    private double hectares;

    @Getter
    private SoilType soilType;

    @Getter
    private LotType type;

    @Setter
    @Getter
    private String description;

    @Getter
    private Polygon polygonLimit;

    @Getter
    private LotState state; // Cambiado a Enum por seguridad

    @Getter
    private Crop crop;

    @Getter
    private List<Animal> animals;

    @Getter
    private List<SpectralMap> spectralMaps;

    private Lot(String name, double hectares, SoilType soilType, LotType type,
                String description, Polygon polygonLimit, LotState state) {
        this.name = name;
        this.hectares = hectares;
        this.soilType = soilType;
        this.type = type;
        this.description = description;
        this.polygonLimit = polygonLimit;
        this.state = state;
        // Inicializamos las relaciones
        this.crop = null;
        this.animals = new ArrayList<>();
        this.spectralMaps = new ArrayList<>();
    }

    public static Lot create(String name, double hectares, SoilType soilType,
                             LotType type, String description, Polygon polygonLimit) {

        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacío");
        }
        if (hectares <= 0) {
            throw new BusinessRuleViolationsException("La cantidad de hectáreas debe ser mayor a 0");
        }
        if (soilType == null) {
            throw new BusinessRuleViolationsException("El tipo de suelo no puede ser nulo");
        }
        if (type == null) {
            throw new BusinessRuleViolationsException("El tipo de lote no puede ser nulo");
        }
        if (polygonLimit == null || polygonLimit.isEmpty()) {
            throw new BusinessRuleViolationsException("El perímetro del lote no puede estar vacío");
        }
        if (!polygonLimit.isValid()) {
            throw new BusinessRuleViolationsException("La geometría del polígono es inválida");
        }

        // Se asigna como ACTIVO por defecto al momento de crearlo
        return new Lot(name, hectares, soilType, type, description, polygonLimit, LotState.ACTIVE);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    public void modifyPolygonLimit(Polygon newPolygonLimit) {
        if (newPolygonLimit == null || newPolygonLimit.isEmpty()) {
            throw new BusinessRuleViolationsException("El nuevo perímetro del lote no puede estar vacío");
        }
        if (!newPolygonLimit.isValid()) {
            throw new BusinessRuleViolationsException("La geometría del nuevo polígono es inválida");
        }
        this.polygonLimit = newPolygonLimit;
        // Reseteamos las hectáreas obligando a que se vuelvan a actualizar tras el cambio
        this.hectares = 0.0;
    }

    public void changeState(LotState newState) {
        if (newState == null) {
            throw new BusinessRuleViolationsException("El nuevo estado no puede ser nulo");
        }
        this.state = newState;
    }

    public void assignCrop(Crop newCrop) {
        if (newCrop == null) {
            throw new BusinessRuleViolationsException("El cultivo a asignar no puede ser nulo");
        }
        this.crop = newCrop;
    }

    public void removeCrop() {
        this.crop = null;
    }

    public void addAnimal(Animal animal) {
        if (animal == null) {
            throw new BusinessRuleViolationsException("El animal no puede ser nulo");
        }
        if (this.animals.contains(animal)) {
            throw new BusinessRuleViolationsException("El animal ya está asignado a este lote");
        }
        this.animals.add(animal);
    }

    public void addSpectralMap(SpectralMap spectralMap) {
        if (spectralMap == null) {
            throw new BusinessRuleViolationsException("El mapa espectral no puede ser nulo");
        }
        this.spectralMaps.add(spectralMap);
    }
}