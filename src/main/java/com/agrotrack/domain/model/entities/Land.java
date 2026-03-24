package com.agrotrack.domain.model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.model.enums.TypeUser;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

public class Land {
    @Setter
    @Getter
    private UUID idLand;
    @Getter
    private List<User> owners;
    @Getter
    private String address;
    @Getter
    private String name;
    @Getter
    private String companyName;
    @Getter
    private String cuit;
    @Getter
    @Setter
    private double surface;
    @Getter
    private Polygon polygonLimit;
    @Getter
    private List<User> assignedStaff;

    private Land(List<User> owners, String address, String name, String companyName, String cuit, Polygon polygonLimit, List<User> assignedStaff) {
        this.owners = owners;
        this.address = address;
        this.name = name;
        this.companyName = companyName;
        this.cuit = cuit;
        this.polygonLimit = polygonLimit;
        if (assignedStaff == null) {
            this.assignedStaff = new ArrayList<>();
        } else{
            this.assignedStaff = assignedStaff;
        }
        this.surface = 0.0;
    }

    public static Land create(List<User> owners, String address,  String name, String companyName, String cuit, Polygon polygonLimit, List<User> assignedStaff) {
        if (owners == null) {
            throw new BusinessRuleViolationsException("La finca debe tener por lo menos un dueño");
        }
        if (address == null || address.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Direccion no puede estar vacio");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre no puede estar vacio");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Nombre de la Empresa no puede estar vacio");
        }
        if (cuit == null || cuit.isBlank()) {
            throw new BusinessRuleViolationsException("El campo Cuit no puede estar vacio");
        }
        if (polygonLimit == null || polygonLimit.isEmpty()){
            throw new BusinessRuleViolationsException("El perimetro de la finca no puede estar vacio");
        }
        if (!polygonLimit.isValid()){
            throw new BusinessRuleViolationsException("La geometria del poligono es invalida");
        }
        return new Land(owners, address, name, companyName, cuit, polygonLimit, assignedStaff);
    }

    public void newOwner(User newOwner) {
        if (!newOwner.getRol().equals(TypeUser.OWNER)){
            throw new BusinessRuleViolationsException("Solo pueden agregarse dueños a aquellos que tengan este rol");
        }
        if (!newOwner.isActive()){
            throw new BusinessRuleViolationsException("Usuario eliminado o inactivo");
        }
        if (owners.contains(newOwner)) {
            throw new BusinessRuleViolationsException("El usuario ya es dueño");
        }
        owners.add(newOwner);
    }

    public void modifyPolygonLimit(Polygon newPolygonLimit) {
        if (newPolygonLimit == null || newPolygonLimit.isEmpty()){
            throw new BusinessRuleViolationsException("El perimetro de la finca no puede estar vacio");
        }
        if (!newPolygonLimit.isValid()){
            throw new BusinessRuleViolationsException("La geometria del poligono es invalida");
        }
        this.polygonLimit = newPolygonLimit;
        this.surface = 0.0;
    }

    public void newWorker(User newWorker){
        if (newWorker.getRol().equals(TypeUser.OWNER) || newWorker.getRol().equals(TypeUser.ADMIN)){
            throw new BusinessRuleViolationsException("Solo pueden agregarse trabajadores a la lista de Staff");
        }
        if (!newWorker.isActive()){
            throw new BusinessRuleViolationsException("Usuario eliminado o inactivo");
        }
        if (assignedStaff.contains(newWorker)){
            throw new BusinessRuleViolationsException("El usuario ya esta asignado como Staff");
        }
        assignedStaff.add(newWorker);
    }
}
