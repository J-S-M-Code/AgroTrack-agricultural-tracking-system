package com.agrotrack.domain.model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.ProductiveOrientation; // Asegúrate de tener este enum
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@Builder
@AllArgsConstructor
public class Farm {
    @Setter
    @Getter
    private UUID idFarm;

    @Getter
    private String name;

    @Getter
    private String companyName;

    @Getter
    private String cuit;

    @Getter
    private String numberRENAPSA;

    @Getter
    private ProductiveOrientation productiveOrientation;

    @Getter
    private String address;

    @Getter
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Polygon polygonLimit;

    @Getter
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point centroid;

    @Getter
    private double surface;

    @Getter
    private String imageUrl;

    @Getter
    private List<Lot> lots;

    @Getter
    private List<Task> tasks;

    @Getter
    private List<Alert> alerts;

    private Farm(String name, String companyName, String cuit, String numberRENAPSA,
                 ProductiveOrientation productiveOrientation, String address,
                 Polygon polygonLimit, Point centroid, double surface, String imageUrl) {
        this.name = name;
        this.companyName = companyName;
        this.cuit = cuit;
        this.numberRENAPSA = numberRENAPSA;
        this.productiveOrientation = productiveOrientation;
        this.address = address;
        this.polygonLimit = polygonLimit;
        this.centroid = centroid;
        this.surface = surface;
        this.imageUrl = imageUrl;
        this.lots = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.alerts = new ArrayList<>();
    }

    public static Farm create(String name, String companyName, String cuit, String numberRENAPSA,
                              ProductiveOrientation productiveOrientation, String address,
                              Polygon polygonLimit, Point centroid, double surface, String imageUrl) {

        // Validaciones de Strings
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacío");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre de la Empresa no puede estar vacío");
        }
        if (cuit == null || cuit.isBlank()) {
            throw new BusinessRuleViolationsException("El campo CUIT no puede estar vacío");
        }
        if (numberRENAPSA == null || numberRENAPSA.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Número RENAPSA no puede estar vacío");
        }
        if (address == null || address.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Dirección no puede estar vacío");
        }

        // Validación de Enums
        if (productiveOrientation == null) {
            throw new BusinessRuleViolationsException("La orientación productiva no puede ser nula");
        }

        // Validaciones Geométricas
        if (polygonLimit == null || polygonLimit.isEmpty()) {
            throw new BusinessRuleViolationsException("El perímetro de la finca no puede estar vacío");
        }
        if (!polygonLimit.isValid()) {
            throw new BusinessRuleViolationsException("La geometría del polígono es inválida");
        }

        // Resolución del Centroide: Si no se proporciona, lo calculamos del polígono
        if (centroid == null || centroid.isEmpty()) {
            centroid = polygonLimit.getCentroid();
        } else if (!centroid.isValid()) {
            throw new BusinessRuleViolationsException("El centroide de la finca proporcionado no es válido");
        }

        // Validación de Superficie
        if (surface <= 0) {
            throw new BusinessRuleViolationsException("La superficie debe ser mayor a 0");
        }

        return new Farm(name, companyName, cuit, numberRENAPSA, productiveOrientation, address,
                polygonLimit, centroid, surface, imageUrl);
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    public void modifyPolygonLimit(Polygon newPolygonLimit) {
        if (newPolygonLimit == null || newPolygonLimit.isEmpty()) {
            throw new BusinessRuleViolationsException("El nuevo perímetro de la finca no puede estar vacío");
        }
        if (!newPolygonLimit.isValid()) {
            throw new BusinessRuleViolationsException("La geometría del nuevo polígono es inválida");
        }
        this.polygonLimit = newPolygonLimit;
        // Al cambiar el polígono, el centroide anterior ya no sirve
        this.centroid = newPolygonLimit.getCentroid();
        // La superficie se resetea para obligar a recalcularla/actualizarla
        this.surface = 0.0;
    }

    public void newLot(Lot newLot) {
        if (newLot == null) {
            throw new BusinessRuleViolationsException("El lote no puede ser nulo");
        }
        if (lots.contains(newLot)) {
            throw new BusinessRuleViolationsException("El lote ya está asignado a esta finca");
        }
        this.lots.add(newLot);
    }

    public void newTask(Task task) {
        if (task == null) {
            throw new BusinessRuleViolationsException("La tarea no puede ser nula");
        }
        if (tasks.contains(task)) {
            throw new BusinessRuleViolationsException("La tarea ya está asignada a esta finca");
        }
        this.tasks.add(task);
    }

    public void newAlert(Alert alert) {
        if (alert == null) {
            throw new BusinessRuleViolationsException("La alerta no puede ser nula");
        }
        if (alerts.contains(alert)) {
            throw new BusinessRuleViolationsException("La alerta ya está asignada a esta finca");
        }
        this.alerts.add(alert); // Se corrigió: en tu versión original faltaba agregarlo a la lista
    }
}