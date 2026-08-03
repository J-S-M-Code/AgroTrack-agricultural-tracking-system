package com.agrotrack.application.dto;

import com.agrotrack.domain.model.enums.LotState;
import com.agrotrack.domain.model.enums.LotType;
import com.agrotrack.domain.model.enums.SoilType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotDto {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private UUID idLot;
    private UUID farmId;
    private String name;
    private double hectares;
    private SoilType soilType;
    private LotType type;
    private String description;
    private PolygonDto polygonLimit;
    private LotState state;
    // Omitimos entidades complejas como Farm o Animals en el DTO básico del lote para evitar bucles de serialización
}
