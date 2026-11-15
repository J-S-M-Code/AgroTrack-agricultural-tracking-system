package com.agrotrack.application.mapper;

import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationDtoMapperTest {

    private final ApplicationDtoMapper mapper = Mappers.getMapper(ApplicationDtoMapper.class);

    @Test
    void testFarmToFarmDto() {
        Polygon polygon = new GeometryFactory().createPolygon(new Coordinate[]{new Coordinate(0,0), new Coordinate(0,1), new Coordinate(1,1), new Coordinate(1,0), new Coordinate(0,0)});
        Farm farm = Farm.create("La Linda", "Company A", "20-11", "REN-1",
                ProductiveOrientation.AGRICULTURAL, "Ruta 9", polygon, polygon.getCentroid(), 150.5, "url123");
        UUID farmId = UUID.randomUUID();
        farm.setIdFarm(farmId);

        // Act
        FarmDto dto = mapper.toFarmDto(farm);

        // Assert
        assertNotNull(dto);
        assertEquals(farmId, dto.getIdFarm());
        assertEquals("La Linda", dto.getName());
        assertEquals(150.5, dto.getSurface());
        assertEquals(ProductiveOrientation.AGRICULTURAL, dto.getProductiveOrientation());
    }
}
