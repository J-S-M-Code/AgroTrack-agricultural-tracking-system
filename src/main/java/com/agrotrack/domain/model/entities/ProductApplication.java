package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class ProductApplication {
    @Getter
    @Setter
    private UUID idApplication;
    @Getter
    private Field field;
    @Getter
    private List<Product> product;
    @Getter
    private User applicator;
    @Getter
    @Setter
    private LocalDateTime applicationDate;
    @Getter
    private Integer waitingTime;
    @Getter
    @Setter
    private String comment;

    private ProductApplication (Field field, List<Product> product, User applicator, String comment, Integer waitingTime){
        this.field = field;
        this.product = product;
        this.applicator = applicator;
        this.comment = comment;
    }

    public static ProductApplication create(Field field, List<Product> product, User applicator, String comment, Integer waitingTime){
        if (field == null){
            throw new BusinessRuleViolationsException("Debe estar asignado a un campo/lote existente");
        }
        if (product == null || product.isEmpty()){
            throw new BusinessRuleViolationsException("Debe tener por lo menos un producto para realizar la tarea");
        }
        if (applicator == null){
            throw new BusinessRuleViolationsException("Debe tener asignado un aplicador");
        }
        if (waitingTime == null || waitingTime<0){
            throw new BusinessRuleViolationsException("El Tiempo de espera no puede ser menor a cero o nulo");
        }
        return new ProductApplication(field, product, applicator, comment, waitingTime);
    }

    public void addProduct(Product newProduct){
        if (newProduct == null){
            throw new BusinessRuleViolationsException("Debe ingresar un producto");
        }
        this.product.add(newProduct);
    }

    public void removeProduct(Product newProduct){
        if (newProduct == null){
            throw new BusinessRuleViolationsException("Debe seleccionar un producto");
        }
        this.product.remove(newProduct);
    }

    public boolean isWaitingPeriodOver(){
        return LocalDateTime.now().isAfter(this.applicationDate.plusDays(waitingTime));
    }
}
