package com.agrotrack.domain.model.entities;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.State; // Asegúrate de tener este enum (ej. ACTIVE, INACTIVE, MAINTENANCE)
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IoTCollar {
    @Getter
    @Setter
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idCollar;

    @Getter
    private String codeRFID;

    @Getter
    private String model;

    @Getter
    private State state;

    @Getter
    private Double batteryLevel;

    @Getter
    private List<GPSPosition> gpsHistory;

    @Getter
    private UUID farmId;

    private IoTCollar(String codeRFID, String model, State state, Double batteryLevel, UUID farmId) {
        this.codeRFID = codeRFID;
        this.model = model;
        this.state = state;
        // Si no se envía un nivel de batería inicial, asumimos que viene cargado al 100%
        this.batteryLevel = (batteryLevel != null) ? batteryLevel : 100.0;
        this.farmId = farmId;
        this.gpsHistory = new ArrayList<>();
    }

    public static IoTCollar create(String codeRFID, String model, State state, Double batteryLevel, UUID farmId) {

        // Validaciones solicitadas
        if (codeRFID == null || codeRFID.isBlank()) {
            throw new BusinessRuleViolationsException("El código RFID del collar no puede estar vacío");
        }
        if (state == null) {
            throw new BusinessRuleViolationsException("El estado del collar no puede ser nulo");
        }
        if (farmId == null) {
            throw new BusinessRuleViolationsException("El ID de la finca no puede ser nulo");
        }

        // Validación extra de seguridad física (la batería debe tener lógica)
        if (batteryLevel != null && (batteryLevel < 0.0 || batteryLevel > 100.0)) {
            throw new BusinessRuleViolationsException("El nivel de batería debe ser un valor entre 0 y 100");
        }

        return new IoTCollar(codeRFID, model, state, batteryLevel, farmId);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    /**
     * Registra una nueva lectura de ubicación enviada por el dispositivo.
     */
    public void addGpsPosition(GPSPosition newPosition) {
        if (newPosition == null) {
            throw new BusinessRuleViolationsException("La posición GPS no puede ser nula");
        }
        this.gpsHistory.add(newPosition);
    }

    /**
     * Actualiza el nivel de batería actual del dispositivo.
     */
    public void updateBatteryLevel(Double currentBattery) {
        if (currentBattery == null || currentBattery < 0.0 || currentBattery > 100.0) {
            throw new BusinessRuleViolationsException("El nivel de batería actualizado debe ser un valor entre 0 y 100");
        }
        this.batteryLevel = currentBattery;
    }

    /**
     * Permite cambiar el estado operativo del collar (por ejemplo, si se rompe o se apaga).
     */
    public void changeState(State newState) {
        if (newState == null) {
            throw new BusinessRuleViolationsException("El nuevo estado no puede ser nulo");
        }
        this.state = newState;
    }

    /**
     * Método de utilidad para saber rápidamente si el collar necesita ser recargado/reemplazado.
     */
    public boolean isLowBattery() {
        // Podrías ajustar este umbral (ej. 15.0%) según las especificaciones del fabricante del collar
        return this.batteryLevel != null && this.batteryLevel <= 15.0;
    }
}