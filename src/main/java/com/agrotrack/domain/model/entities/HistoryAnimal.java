package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

public class HistoryAnimal {
    @Getter
    @Setter
    private UUID idHistory;
    @Getter
    private Animal animal;
    @Getter
    private Point position;
    @Getter
    private LocalDateTime date;

    private HistoryAnimal(Animal animal, Point position, LocalDateTime date){
        this.animal = animal;
        this.position = position;
        this.date = date;
    }

    public static HistoryAnimal createHistoryAnimal(Animal animal, Point position, LocalDateTime date){
        if (animal == null){
           throw new BusinessRuleViolationsException("Error: Animal no asignado");
        }
        if (date == null){
            throw new BusinessRuleViolationsException("Error: fecha no asignado");
        }
        validatePosition(position);
        return new HistoryAnimal(animal, position, date);
    }

    private static void validatePosition(Point position) {
        // 1. Validación de nulidad (La que ya tienes)
        if (position == null) {
            throw new BusinessRuleViolationsException("Error: Posición no asignada.");
        }

        // 2. Validación de geometría vacía
        if (position.isEmpty()) {
            throw new BusinessRuleViolationsException("Error: La posición no contiene coordenadas válidas (Point Empty).");
        }

        // 3. Validación de validez matemática
        // Asegura que la estructura de la geometría no esté corrupta.
        if (!position.isValid()) {
            throw new BusinessRuleViolationsException("Error: La geometría de la posición es inválida.");
        }

        // 4. Validación de Límites Geográficos
        double longitude = position.getX();
        double latitude = position.getY();

        // La Longitud válida en el globo terrestre va de -180 a 180 grados
        if (longitude < -180 || longitude > 180) {
            throw new BusinessRuleViolationsException("Error: La longitud georreferenciada (" + longitude + ") está fuera de los límites terrestres permitidos (-180 a 180).");
        }

        // La Latitud válida va de -90 a 90 grados
        if (latitude < -90 || latitude > 90) {
            throw new BusinessRuleViolationsException("Error: La latitud georreferenciada (" + latitude + ") está fuera de los límites terrestres permitidos (-90 a 90).");
        }
    }
}
