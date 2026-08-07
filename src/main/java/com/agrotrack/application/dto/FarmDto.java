package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.ProductiveOrientation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmDto {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idFarm;
    private String name;
    private String companyName;
    private String cuit;
    private String numberRENAPSA;
    private ProductiveOrientation productiveOrientation;
    private String address;
    private PolygonDto polygonLimit;
    private PointDto centroid;
    private double surface;
    private String imageUrl;
    private String myRole;
}
