package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.HealthEventType; // Asegúrate de tener este enum
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealthEvent {
    @Getter
    @Setter
    private UUID idHealthEvent;

    @Getter
    private LocalDateTime date;

    @Getter
    private HealthEventType type;

    @Getter
    private String treatment;

    @Getter
    private String numAct;

    @Getter
    private String observation;

    @Getter
    private List<String> images;

    @Getter
    private User veterinarian;

    private HealthEvent(LocalDateTime date, HealthEventType type, String treatment,
                        String numAct, String observation, User veterinarian) {
        this.date = date;
        this.type = type;
        this.treatment = treatment;
        this.numAct = numAct;
        this.observation = observation;
        this.veterinarian = veterinarian;
        this.images = new ArrayList<>();
    }

    public static HealthEvent create(LocalDateTime date, HealthEventType type, String treatment,
                                     String numAct, String observation, User veterinarian) {

        // Validación de fecha
        if (date == null) {
            throw new BusinessRuleViolationsException("La fecha del evento de salud no puede ser nula");
        }
        if (date.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationsException("La fecha del evento de salud no puede ser en el futuro");
        }

        // Validación de Enums y Entidades
        if (type == null) {
            throw new BusinessRuleViolationsException("El tipo de evento de salud no puede ser nulo");
        }
        if (veterinarian == null) {
            throw new BusinessRuleViolationsException("El veterinario responsable no puede ser nulo");
        }

        // Validación de Strings
        if (treatment == null || treatment.isBlank()) {
            throw new BusinessRuleViolationsException("El tratamiento no puede estar vacío");
        }
        if (numAct == null || numAct.isBlank()) {
            throw new BusinessRuleViolationsException("El número de acta/registro no puede estar vacío");
        }
        if (observation == null || observation.isBlank()) {
            throw new BusinessRuleViolationsException("La observación no puede estar vacía");
        }

        return new HealthEvent(date, type, treatment, numAct, observation, veterinarian);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    /**
     * Permite agregar una imagen (URL o ruta) como evidencia del evento de salud
     * (ej. foto de la receta o del estado del animal).
     */
    public void addImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessRuleViolationsException("La URL de la imagen no puede estar vacía");
        }
        this.images.add(imageUrl);
    }

    /**
     * Permite anexar o actualizar la observación si el veterinario necesita agregar
     * información sobre la evolución del animal a este mismo registro.
     */
    public void updateObservation(String newObservation) {
        if (newObservation == null || newObservation.isBlank()) {
            throw new BusinessRuleViolationsException("La nueva observación no puede estar vacía");
        }
        this.observation = newObservation;
    }
}