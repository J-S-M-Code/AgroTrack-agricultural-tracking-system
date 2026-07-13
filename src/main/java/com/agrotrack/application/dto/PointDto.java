package com.agrotrack.application.dto;

import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

@Data
public class PointDto {
    private String type = "Point";
    private List<Double> coordinates;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Point toJtsPoint() {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(coordinates.get(0), coordinates.get(1)));
    }

    public static PointDto fromJtsPoint(Point point) {
        if (point == null) return null;
        PointDto dto = new PointDto();
        dto.setType("Point");
        dto.setCoordinates(List.of(point.getX(), point.getY()));
        return dto;
    }
}
