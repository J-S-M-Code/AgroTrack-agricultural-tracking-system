package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


public class Product {
    @Getter
    @Setter
    private UUID idProduct;
    @Getter
    private String numSenasa;
    @Getter
    private String activeIngredient;
    @Getter
    private String unitMeasurement;
    @Getter
    @Setter
    private double amunt;
    @Getter
    private Integer waitingTime;

    private Product(String numSenasa, String activeIngredient, String unitMeasurement, double amunt, Integer waitingTime){
        this.numSenasa = numSenasa;
        this.activeIngredient = activeIngredient;
        this.unitMeasurement = unitMeasurement;
        this.amunt = amunt;
        this.waitingTime = waitingTime;
    }

    public static Product create (String numSenasa, String activeIngredient, String unitMeasurement, double amunt, Integer waitingTime){
        if (numSenasa.isBlank() || numSenasa == null){
            throw new BusinessRuleViolationsException("El campo Numero SENASA no puede estar vacio.");
        }
        if (activeIngredient.isBlank() || activeIngredient == null){
            throw new BusinessRuleViolationsException("El campo Ingrediente Activo no puede estar vacio.");
        }
        if (unitMeasurement.isBlank() || unitMeasurement == null){
            throw new BusinessRuleViolationsException("El campo Unidad de Medida no puede estar vacio");
        }
        if (amunt <= 0 ){
            throw new BusinessRuleViolationsException("El campo Cantidad no puede ser 0 o menor a 0");
        }
        if  (waitingTime < 0){
            throw new BusinessRuleViolationsException("El campo Timepo de Espera no puede ser menor a 0");
        }
        return new Product(numSenasa, activeIngredient, unitMeasurement, amunt, waitingTime);
    }
}
