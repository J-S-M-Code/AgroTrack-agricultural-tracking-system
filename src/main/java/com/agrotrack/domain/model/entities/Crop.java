package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.PhenologicalState;
import com.agrotrack.domain.model.enums.TypeCrop;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class Crop {
    @Getter
    @Setter
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idCrop;

    @Getter
    private TypeCrop typeCrop;

    @Getter
    private String species;

    @Getter
    private String variety;

    @Getter
    private LocalDateTime plantingDate;

    @Getter
    private LocalDateTime estimateHarvestDate;

    @Getter
    private Lot assignedLot;

    @Getter
    private double implantedSurface;

    @Getter
    private String renspa;

    @Getter
    private PhenologicalState phenologicalState;

    private Crop(TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                 LocalDateTime estimateHarvestDate, Lot assignedLot, double implantedSurface,
                 String renspa, PhenologicalState phenologicalState) {
        this.typeCrop = typeCrop;
        this.species = species;
        this.variety = variety;
        this.plantingDate = plantingDate;
        this.estimateHarvestDate = estimateHarvestDate;
        this.assignedLot = assignedLot;
        this.implantedSurface = implantedSurface;
        this.renspa = renspa;
        this.phenologicalState = phenologicalState;
    }

    public static Crop create(TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                              LocalDateTime estimateHarvestDate, Lot assignedLot, double implantedSurface,
                              String renspa, PhenologicalState phenologicalState) {

        // Validaciones de Enums y Entidades
        if (typeCrop == null) {
            throw new BusinessRuleViolationsException("El tipo de cultivo no puede ser nulo");
        }
        if (assignedLot == null) {
            throw new BusinessRuleViolationsException("El cultivo debe estar asignado a un lote");
        }
        if (phenologicalState == null) {
            throw new BusinessRuleViolationsException("El estado fenológico no puede ser nulo");
        }

        // Validaciones de Strings
        if (species == null || species.isBlank()) {
            throw new BusinessRuleViolationsException("La especie no puede estar vacía");
        }
        if (variety == null || variety.isBlank()) {
            throw new BusinessRuleViolationsException("La variedad no puede estar vacía");
        }
        if (renspa == null || renspa.isBlank()) {
            throw new BusinessRuleViolationsException("El código RENSPA no puede estar vacío");
        }

        // Validaciones de Fechas
        if (plantingDate == null) {
            throw new BusinessRuleViolationsException("La fecha de plantación no puede ser nula");
        }
        if (estimateHarvestDate == null) {
            throw new BusinessRuleViolationsException("La fecha estimada de cosecha no puede ser nula");
        }
        if (estimateHarvestDate.isBefore(plantingDate)) {
            throw new BusinessRuleViolationsException("La fecha de cosecha estimada no puede ser anterior a la fecha de plantación");
        }

        // Validaciones de Números
        if (implantedSurface <= 0) {
            throw new BusinessRuleViolationsException("La superficie implantada debe ser mayor a 0");
        }
        // Validacion cruzada opcional: Verificar que la superficie implantada no supere la del lote
        if (implantedSurface > assignedLot.getHectares()) {
            throw new BusinessRuleViolationsException("La superficie implantada no puede superar la superficie total del lote asignado");
        }

        return new Crop(typeCrop, species, variety, plantingDate, estimateHarvestDate, assignedLot, implantedSurface, renspa, phenologicalState);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    public void setHarvestDate(LocalDateTime harvest) {
        if (harvest == null) {
            throw new BusinessRuleViolationsException("La nueva fecha de cosecha no puede ser nula");
        }
        if (harvest.isBefore(this.plantingDate)) {
            throw new BusinessRuleViolationsException("La nueva fecha de cosecha no puede ser anterior a la fecha de plantación");
        }
        this.estimateHarvestDate = harvest;
    }

    public boolean isReadyForHarvest(LocalDateTime currentDate) {
        if (currentDate == null) {
            throw new BusinessRuleViolationsException("La fecha actual para calcular la cosecha no puede ser nula");
        }
        // Está listo para cosechar si la fecha actual es igual o posterior a la fecha estimada
        return !currentDate.isBefore(this.estimateHarvestDate);
    }

    public void updatePhenologicalState(PhenologicalState newState) {
        if (newState == null) {
            throw new BusinessRuleViolationsException("El nuevo estado fenológico no puede ser nulo");
        }
        this.phenologicalState = newState;
    }

    public void update(TypeCrop typeCrop, String species, String variety, LocalDateTime plantingDate,
                       LocalDateTime estimateHarvestDate, Lot assignedLot, double implantedSurface,
                       String renspa, PhenologicalState phenologicalState) {
        
        if (typeCrop == null) throw new BusinessRuleViolationsException("El tipo de cultivo no puede ser nulo");
        if (assignedLot == null) throw new BusinessRuleViolationsException("El cultivo debe estar asignado a un lote");
        if (phenologicalState == null) throw new BusinessRuleViolationsException("El estado fenológico no puede ser nulo");
        if (species == null || species.isBlank()) throw new BusinessRuleViolationsException("La especie no puede estar vacía");
        if (variety == null || variety.isBlank()) throw new BusinessRuleViolationsException("La variedad no puede estar vacía");
        if (renspa == null || renspa.isBlank()) throw new BusinessRuleViolationsException("El código RENSPA no puede estar vacío");
        if (plantingDate == null) throw new BusinessRuleViolationsException("La fecha de plantación no puede ser nula");
        if (estimateHarvestDate == null) throw new BusinessRuleViolationsException("La fecha estimada de cosecha no puede ser nula");
        if (estimateHarvestDate.isBefore(plantingDate)) throw new BusinessRuleViolationsException("La fecha de cosecha estimada no puede ser anterior a la fecha de plantación");
        if (implantedSurface <= 0) throw new BusinessRuleViolationsException("La superficie implantada debe ser mayor a 0");
        if (implantedSurface > assignedLot.getHectares()) throw new BusinessRuleViolationsException("La superficie implantada no puede superar la superficie total del lote asignado");

        this.typeCrop = typeCrop;
        this.species = species;
        this.variety = variety;
        this.plantingDate = plantingDate;
        this.estimateHarvestDate = estimateHarvestDate;
        this.assignedLot = assignedLot;
        this.implantedSurface = implantedSurface;
        this.renspa = renspa;
        this.phenologicalState = phenologicalState;
    }
}