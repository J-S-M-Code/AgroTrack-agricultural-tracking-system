package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.Species; // Asegúrate de crear este enum
import com.agrotrack.domain.model.enums.CategoryAnimal;
import com.agrotrack.domain.model.enums.Sex;
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
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idAnimal;

    @Getter
    @Setter
    private boolean active = true;

    @Getter
    @Setter
    private String deletionReason;

    @Getter
    private String visualCaravan;

    @Getter
    private String caravanSenasa;

    @Getter
    private String livestockKey;

    @Getter
    private String numRENSPA;

    @Getter
    private String internalManagementCaravan;

    @Getter
    private IoTCollar collar;

    @Getter
    private Species species;

    @Getter
    private String race;

    @Getter
    private Sex sex;

    @Getter
    private CategoryAnimal category;

    @Getter
    private LocalDateTime birthdate;

    @Getter
    private double currentWeight;

    @Getter
    private List<HealthEvent> healthHistory;

    @Getter
    private List<AnimalMovement> movementHistory;

    @Getter
    private Lot assignedLot;

    private Animal(String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                   String internalManagementCaravan, Species species, String race, Sex sex,
                   CategoryAnimal category, LocalDateTime birthdate, double currentWeight, Lot assignedLot) {
        this.visualCaravan = visualCaravan;
        this.caravanSenasa = caravanSenasa;
        this.livestockKey = livestockKey;
        this.numRENSPA = numRENSPA;
        this.internalManagementCaravan = internalManagementCaravan;
        this.species = species;
        this.race = race;
        this.sex = sex;
        this.category = category;
        this.birthdate = birthdate;
        this.currentWeight = currentWeight;
        this.assignedLot = assignedLot;

        // Inicializamos las relaciones
        this.collar = null;
        this.healthHistory = new ArrayList<>();
        this.movementHistory = new ArrayList<>();
    }

    public static Animal create(String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                                String internalManagementCaravan, Species species, String race, Sex sex,
                                CategoryAnimal category, LocalDateTime birthdate, double currentWeight, Lot assignedLot) {

        // Validaciones de Strings
        if (visualCaravan == null || visualCaravan.isBlank()) {
            throw new BusinessRuleViolationsException("La caravana visual no puede estar vacía");
        }
        if (caravanSenasa == null || caravanSenasa.isBlank()) {
            throw new BusinessRuleViolationsException("La caravana SENASA no puede estar vacía");
        }
        if (livestockKey == null || livestockKey.isBlank()) {
            throw new BusinessRuleViolationsException("La clave ganadera no puede estar vacía");
        }
        if (numRENSPA == null || numRENSPA.isBlank()) {
            throw new BusinessRuleViolationsException("El número RENSPA no puede estar vacío");
        }
        if (race == null || race.isBlank()) {
            throw new BusinessRuleViolationsException("La raza no puede estar vacía");
        }

        // Validaciones de Enums y Entidades
        if (species == null) {
            throw new BusinessRuleViolationsException("Debe seleccionar la especie del animal");
        }
        if (sex == null) {
            throw new BusinessRuleViolationsException("Debe seleccionar el sexo del animal");
        }
        if (category == null) {
            throw new BusinessRuleViolationsException("Debe seleccionar la categoría del animal");
        }
        if (assignedLot == null) {
            throw new BusinessRuleViolationsException("El animal debe estar asignado a un lote");
        }

        // Validaciones de Fecha y Números
        if (birthdate == null) {
            throw new BusinessRuleViolationsException("La fecha de nacimiento no puede ser nula");
        }
        if (birthdate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("La fecha de nacimiento no puede ser mayor a la fecha actual");
        }
        if (currentWeight <= 0) {
            throw new BusinessRuleViolationsException("El peso actual debe ser mayor a 0");
        }

        return new Animal(visualCaravan, caravanSenasa, livestockKey, numRENSPA, internalManagementCaravan,
                species, race, sex, category, birthdate, currentWeight, assignedLot);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    public void changeTypeCategory(CategoryAnimal newCategory) {
        if (newCategory == null) {
            throw new BusinessRuleViolationsException("La nueva categoría no puede ser nula");
        }
        this.category = newCategory;
    }

    public int calculateAgeInMonths(LocalDateTime currentDate) {
        if (currentDate == null) {
            throw new BusinessRuleViolationsException("La fecha actual no puede ser nula para calcular la edad");
        }
        if (currentDate.isBefore(this.birthdate)) {
            throw new BusinessRuleViolationsException("La fecha proporcionada no puede ser anterior a la fecha de nacimiento");
        }
        return Math.toIntExact(ChronoUnit.MONTHS.between(this.birthdate, currentDate));
    }

    // --- Métodos extra que te pueden ser útiles para gestionar las nuevas entidades ---

    public void assignCollar(IoTCollar newCollar) {
        if (this.collar != null) {
            this.collar.changeState(com.agrotrack.domain.model.enums.State.AVAILABLE);
        }
        if (newCollar != null) {
            newCollar.changeState(com.agrotrack.domain.model.enums.State.ASSIGNED);
        }
        this.collar = newCollar;
    }

    public void updateWeight(double newWeight) {
        if (newWeight <= 0) {
            throw new BusinessRuleViolationsException("El nuevo peso debe ser mayor a 0");
        }
        this.currentWeight = newWeight;
    }

    public void moveToLot(Lot newLot, AnimalMovement newMovement) {
        if (newLot == null) {
            throw new BusinessRuleViolationsException("El nuevo lote no puede ser nulo");
        }
        if (newMovement == null) {
            throw new BusinessRuleViolationsException("El registro del movimiento no puede ser nulo");
        }
        this.assignedLot = newLot;
        this.movementHistory.add(newMovement);
    }

    public void update(String visualCaravan, String caravanSenasa, String livestockKey, String numRENSPA,
                       String internalManagementCaravan, Species species, String race, Sex sex,
                       CategoryAnimal category, LocalDateTime birthdate, double currentWeight, Lot assignedLot) {
        
        if (visualCaravan == null || visualCaravan.isBlank()) throw new BusinessRuleViolationsException("La caravana visual no puede estar vacía");
        if (caravanSenasa == null || caravanSenasa.isBlank()) throw new BusinessRuleViolationsException("La caravana SENASA no puede estar vacía");
        if (livestockKey == null || livestockKey.isBlank()) throw new BusinessRuleViolationsException("La clave ganadera no puede estar vacía");
        if (numRENSPA == null || numRENSPA.isBlank()) throw new BusinessRuleViolationsException("El número RENSPA no puede estar vacío");
        if (race == null || race.isBlank()) throw new BusinessRuleViolationsException("La raza no puede estar vacía");
        if (species == null) throw new BusinessRuleViolationsException("Debe seleccionar la especie del animal");
        if (sex == null) throw new BusinessRuleViolationsException("Debe seleccionar el sexo del animal");
        if (category == null) throw new BusinessRuleViolationsException("Debe seleccionar la categoría del animal");
        if (birthdate == null) throw new BusinessRuleViolationsException("La fecha de nacimiento no puede ser nula");
        if (birthdate.isAfter(LocalDateTime.now())) throw new BusinessRuleViolationsException("La fecha de nacimiento no puede ser mayor a la fecha actual");
        if (currentWeight <= 0) throw new BusinessRuleViolationsException("El peso actual debe ser mayor a 0");

        this.visualCaravan = visualCaravan;
        this.caravanSenasa = caravanSenasa;
        this.livestockKey = livestockKey;
        this.numRENSPA = numRENSPA;
        this.internalManagementCaravan = internalManagementCaravan;
        this.species = species;
        this.race = race;
        this.sex = sex;
        this.category = category;
        this.birthdate = birthdate;
        this.currentWeight = currentWeight;
        this.assignedLot = assignedLot;
    }
}