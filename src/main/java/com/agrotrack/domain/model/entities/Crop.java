package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeCrop;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Crop {
    @Setter
    private UUID idCrop;

    private Field field;
    private TypeCrop typeCrop;
    private LocalDateTime plantingDate;
    private Integer monthsCultivate;
    private Integer monthsBetweenHarvests;
    private LocalDateTime estimatedHarvestDate;
    private boolean isActive;

    private Crop(Field field, TypeCrop typeCrop, LocalDateTime plantingDate, Integer monthsCultivate, Integer monthsBetweenHarvests) {
        this.field = field;
        this.typeCrop = typeCrop;
        this.plantingDate = plantingDate;
        this.monthsCultivate = monthsCultivate;
        this.monthsBetweenHarvests = monthsBetweenHarvests;
        this.estimatedHarvestDate = plantingDate.plusMonths(monthsCultivate);
        this.isActive = true;
    }

    public static Crop create(Field field, TypeCrop typeCrop, LocalDateTime plantingDate, Integer monthsCultivate, Integer monthsBetweenHarvests) {
        if (field == null) throw new BusinessRuleViolationsException("Debe estar asignado a un campo/lote existente");
        if (typeCrop == null) throw new BusinessRuleViolationsException("Debe tener un tipo de cultivo asignado");
        if (plantingDate == null) throw new BusinessRuleViolationsException("Seleccione una fecha de plantación");
        if (monthsCultivate == null || monthsCultivate < 0) throw new BusinessRuleViolationsException("Los meses para cultivar deben ser mayor o igual a 0");

        // Regla de negocio: Si es FRUIT, debe tener un intervalo de recosecha.
        if (isPerennial(typeCrop) && (monthsBetweenHarvests == null || monthsBetweenHarvests <= 0)) {
            throw new BusinessRuleViolationsException("Los cultivos perennes requieren definir los meses entre cosechas.");
        }

        return new Crop(field, typeCrop, plantingDate, monthsCultivate, monthsBetweenHarvests);
    }

    /**
     * Registrar una cosecha y preparar el siguiente ciclo si corresponde.
     */
    public void registerHarvest(LocalDateTime actualHarvestDate) {
        if (!this.isActive) {
            throw new BusinessRuleViolationsException("No se puede cosechar un cultivo que ya está inactivo o finalizado.");
        }

        if (isPerennial(this.typeCrop)) {
            // Si es frutal o forraje, calculamos la próxima fecha de cosecha basándonos en la fecha actual
            this.estimatedHarvestDate = actualHarvestDate.plusMonths(this.monthsBetweenHarvests);
        } else {
            // Si es un cultivo anual, el ciclo de vida termina con la cosecha
            this.isActive = false;
        }
    }

    private static boolean isPerennial(TypeCrop typeCrop) {
        return typeCrop == TypeCrop.FRUIT || typeCrop == TypeCrop.FORAGE;
    }
}