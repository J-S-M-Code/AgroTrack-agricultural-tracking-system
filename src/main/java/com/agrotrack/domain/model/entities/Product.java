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
    private String name;
    @Getter
    private String numSenasa;
    @Getter
    private String activeIngredient;
    @Getter
    private String unitMeasurement;
    @Getter
    private double stockQuantity;
    @Getter
    private Integer waitingTime;

    private Product(String name, String numSenasa, String activeIngredient, String unitMeasurement, double stockQuantity,
                    Integer waitingTime){
        this.name = name;
        this.numSenasa = numSenasa;
        this.activeIngredient = activeIngredient;
        this.unitMeasurement = unitMeasurement;
        this.stockQuantity = stockQuantity;
        this.waitingTime = waitingTime;
    }

    public static Product create (String name, String numSenasa, String activeIngredient, String unitMeasurement,
                                  double stockQuantity, Integer waitingTime){
        if (name == null || name.isBlank()){
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacio");
        }
        if (numSenasa == null || numSenasa.isBlank()){
            throw new BusinessRuleViolationsException("El campo Numero SENASA no puede estar vacio.");
        }
        if (activeIngredient == null || activeIngredient.isBlank()){
            throw new BusinessRuleViolationsException("El campo Ingrediente Activo no puede estar vacio.");
        }
        if (unitMeasurement == null || unitMeasurement.isBlank()){
            throw new BusinessRuleViolationsException("El campo Unidad de Medida no puede estar vacio");
        }
        if (stockQuantity <= 0 ){
            throw new BusinessRuleViolationsException("El campo Cantidad no puede ser 0 o menor a 0");
        }
        if  (waitingTime < 0){
            throw new BusinessRuleViolationsException("El campo Timepo de Espera no puede ser menor a 0");
        }
        return new Product(name, numSenasa, activeIngredient, unitMeasurement, stockQuantity, waitingTime);
    }

    public void addStock(double plusStock){
        if (plusStock <= 0){
            throw new BusinessRuleViolationsException("El campo de nueva cantidad no puede ser 0 o menor a 0");
        }
        this.stockQuantity += plusStock;
    }

    public void consumeStock(double consumeStock){
        if (consumeStock <= 0){
            throw new BusinessRuleViolationsException("El campo cantidad consumida no puede ser 0 o menor a 0");
        }
        if (consumeStock >= stockQuantity){
            throw new BusinessRuleViolationsException("La cantidad consumida ingresada es mayor que la existente");
        }
        this.stockQuantity -= consumeStock;
    }
}
