package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class AnimalMovement {
    @Getter
    @Setter
    private UUID idMovement;
    @Getter
    private Animal animal;
    @Getter
    private Field field;
    @Getter
    private LocalDateTime entryDate;
    @Getter
    @Setter
    private LocalDateTime exitDate;

    private AnimalMovement(Animal animal, Field field, LocalDateTime entryDate){
        this.animal = animal;
        this.field = field;
        this.entryDate = entryDate;
    }

    public static AnimalMovement create(Animal animal, Field field, LocalDateTime entryDate){
        if(animal == null){
            throw new BusinessRuleViolationsException("Debe seleccionar un animal");
        }
        if (field == null){
            throw new BusinessRuleViolationsException("Debes seleccionar un campo");
        }
        if (entryDate == null || entryDate.isAfter(LocalDateTime.now())){
            throw new BusinessRuleViolationsException("Fecha de ingreso no es correcta");
        }
        return new AnimalMovement(animal, field, entryDate);
    }
}
