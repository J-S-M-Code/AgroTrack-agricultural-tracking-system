package com.agrotrack.domain.model.entities;


import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeCategoryAnimal;
import com.agrotrack.domain.model.enums.TypeSex;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Animal {
    @Getter
    @Setter
    private UUID idAnimal;
    @Getter
    private Field field;
    @Getter
    private String caravanSenasa;
    @Getter
    private String caravanManagement;
    @Getter
    private String rfidTag;
    @Getter
    private String race;
    @Getter
    private TypeSex sex;
    @Getter
    @Setter
    private TypeCategoryAnimal category;
    @Getter
    private LocalDateTime birthdate;
    @Getter
    @Setter
    private double currentWeight;
    @Getter
    private List<AnimalMovement> animalMovement;

    private Animal(Field field, String caravanSenasa, String caravanManagement, String race, TypeSex sex,
                   TypeCategoryAnimal category, LocalDateTime birthdate, double currentWeight, AnimalMovement animalMovement) {
        this.field = field;
        this.caravanSenasa = caravanSenasa;
        this.caravanManagement = caravanManagement;
        this.race = race;
        this.sex = sex;
        this.category = category;
        this.birthdate = birthdate;
        this.currentWeight = currentWeight;
        this.animalMovement = new ArrayList<>();
        this.animalMovement.add(animalMovement);
    }

    public static Animal create(Field field, String caravanSenasa, String caravanManagement, String race, TypeSex sex,
                                TypeCategoryAnimal category, LocalDateTime birthdate, double currentWeight, AnimalMovement animalMovement){
        if (field == null) {
            throw new BusinessRuleViolationsException("Debe estar asignado a un campo/lote existente");
        }
        if (caravanSenasa== null || caravanSenasa.isBlank()){
            throw new BusinessRuleViolationsException("El campo Caravana SENASA no puede estar vacio");
        }
        if (caravanManagement == null || caravanManagement.isBlank()){
            throw new BusinessRuleViolationsException("El campo Caravana no puede estar vacio");
        }
        if (race == null || race.isBlank()){
            throw new BusinessRuleViolationsException("El campo Raza no puede estar vacio");
        }
        if (sex == null){
            throw new BusinessRuleViolationsException("Debe seleccionar el sexo del animal");
        }
        if (category == null){
            throw new BusinessRuleViolationsException("Debe seleccionar el categoria del animal");
        }
        if (birthdate == null || birthdate.isAfter(LocalDateTime.now())){
            throw new BusinessRuleViolationsException("La fecha de nacimiento no es valida");
        }
        if (currentWeight <= 0 || currentWeight > 9999){
            throw new BusinessRuleViolationsException("Ingrese un peso valido");
        }
        if (animalMovement == null){
            throw new BusinessRuleViolationsException("Se debe ingresar el campo/lote donde va a estar el animal");
        }
        return new Animal(field, caravanSenasa, caravanManagement, race, sex, category, birthdate, currentWeight, animalMovement);
    }

    public void assignRfidTag(String newRfidTag) {
        if (newRfidTag == null || newRfidTag.isBlank()) {
            throw new BusinessRuleViolationsException("El código RFID no puede ser nulo o vacío");
        }
        this.rfidTag = newRfidTag;
    }

    public void newMovement(AnimalMovement newMovement){
        this.animalMovement.add(newMovement);
    }

    public int ageInMonths(){
        if (this.birthdate == null) return 0;
        return Math.toIntExact(ChronoUnit.MONTHS.between(this.birthdate, LocalDateTime.now()));
    }
}
