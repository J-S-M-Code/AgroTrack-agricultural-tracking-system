package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.ProductiveOrientation;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Data
@Builder
public class FarmDto {
    private UUID idFarm;
    private String name;
    private String companyName;
    private String cuit;
    private String numberRENAPSA;
    private ProductiveOrientation productiveOrientation;
    private String address;
    private Polygon polygonLimit;
    private Point centroid;
    private double surface;
    private String imageUrl;
}
