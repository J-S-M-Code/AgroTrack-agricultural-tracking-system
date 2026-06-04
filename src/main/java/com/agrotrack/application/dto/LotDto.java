package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Data
@Builder
public class LotDto {
    private UUID idLot;
    private String name;
    private double hectares;
    private SoilType soilType;
    private LotType type;
    private String description;
    private Polygon polygonLimit;
    private LotState state;
    // Omitimos entidades complejas como Farm o Animals en el DTO básico del lote para evitar bucles de serialización
}
