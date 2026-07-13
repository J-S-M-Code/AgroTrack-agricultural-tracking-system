package com.agrotrack.application.dto;

import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.ArrayList;
import java.util.List;

@Data
public class PolygonDto {
    private String type = "Polygon";
    private List<List<List<Double>>> coordinates;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Polygon toJtsPolygon() {
        if (coordinates == null || coordinates.isEmpty() || coordinates.get(0).isEmpty()) {
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] coords = new Coordinate[coordinates.get(0).size()];
        for (int i = 0; i < coordinates.get(0).size(); i++) {
            List<Double> point = coordinates.get(0).get(i);
            coords[i] = new Coordinate(point.get(0), point.get(1));
        }
        return geometryFactory.createPolygon(coords);
    }

    public static PolygonDto fromJtsPolygon(Polygon polygon) {
        if (polygon == null) return null;
        PolygonDto dto = new PolygonDto();
        dto.setType("Polygon");
        Coordinate[] coords = polygon.getCoordinates();
        List<List<Double>> ring = new ArrayList<>();
        for (Coordinate coord : coords) {
            ring.add(List.of(coord.getX(), coord.getY()));
        }
        dto.setCoordinates(List.of(ring));
        return dto;
    }
}
