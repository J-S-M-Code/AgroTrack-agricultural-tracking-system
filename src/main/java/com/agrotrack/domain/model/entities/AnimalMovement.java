package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class AnimalMovement {
    @Getter
    @Setter
    private UUID idMovement;

    @Getter
    private Lot originLot;

    @Getter
    private LocalDateTime entryDate;

    @Getter
    private LocalDateTime exitDate;

    @Getter
    private User registeredBy;

    private AnimalMovement(Lot originLot, LocalDateTime entryDate, User registeredBy) {
        this.originLot = originLot;
        this.entryDate = entryDate;
        this.registeredBy = registeredBy;
        this.exitDate = null; // Inicialmente el movimiento está "abierto"
    }

    public static AnimalMovement create(Lot originLot, LocalDateTime entryDate, User registeredBy) {

        if (originLot == null) {
            throw new BusinessRuleViolationsException("El lote de origen no puede ser nulo");
        }

        if (entryDate == null) {
            throw new BusinessRuleViolationsException("La fecha de entrada no puede ser nula");
        }

        if (entryDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("La fecha de entrada no puede ser una fecha futura");
        }

        if (registeredBy == null) {
            throw new BusinessRuleViolationsException("Debe especificarse el usuario que registra el movimiento");
        }

        return new AnimalMovement(originLot, entryDate, registeredBy);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    /**
     * Cierra el movimiento actual asignando una fecha de salida.
     * @param exitDate Fecha en la que el animal efectivamente dejó el lote.
     */
    public void closeMovement(LocalDateTime exitDate) {
        if (exitDate == null) {
            throw new BusinessRuleViolationsException("La fecha de salida no puede ser nula al cerrar un movimiento");
        }

        if (exitDate.isBefore(this.entryDate)) {
            throw new BusinessRuleViolationsException("La fecha de salida no puede ser anterior a la fecha de entrada");
        }

        if (exitDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("La fecha de salida no puede ser una fecha futura");
        }

        this.exitDate = exitDate;
    }

    /**
     * Calcula cuántos días permaneció el animal en el lote.
     * Si el movimiento no ha cerrado, calcula el tiempo hasta la fecha actual.
     */
    public long getDurationInDays(LocalDateTime currentDate) {
        LocalDateTime end = (this.exitDate != null) ? this.exitDate : currentDate;
        return Duration.between(this.entryDate, end).toDays();
    }

    /**
     * Indica si el animal todavía se encuentra en este lote (movimiento activo).
     */
    public boolean isActive() {
        return this.exitDate == null;
    }
}