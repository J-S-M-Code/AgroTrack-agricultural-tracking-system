package com.agrotrack.infrastructure.adapters.out;

import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.enums.ProductiveOrientation;
import com.agrotrack.infrastructure.adapters.out.database.repositories.FarmJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class FarmRepositoryAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:15-3.3").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private FarmJpaRepository farmJpaRepository;

    private FarmRepositoryAdapter adapter;

    private final GeometryFactory geometryFactory = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);
    private Polygon samplePolygon;

    @BeforeEach
    void setUp() {
        adapter = new FarmRepositoryAdapter(farmJpaRepository);

        samplePolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(10, 0),
                new Coordinate(0, 0)
        });
    }

    @Test
    void testSaveAndFindById() {
        // Arrange
        Farm farm = Farm.create(
                "Finca IT", "Corp IT", "30-55555555-5", "REN1",
                ProductiveOrientation.AGRICULTURAL, "Dir", samplePolygon, samplePolygon.getCentroid(), 100.0, "url"
        );

        // Act
        Farm saved = adapter.save(farm);
        Optional<Farm> retrieved = adapter.findById(saved.getIdFarm());

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("Finca IT", retrieved.get().getName());
        assertEquals("30-55555555-5", retrieved.get().getCuit());
        assertNotNull(retrieved.get().getPolygonLimit(), "Polygon was not saved correctly");
    }

    @Test
    void testExistsOverlappingFarm() {
        // Arrange
        Farm farm1 = Farm.create("F1", "C", "C1", "R1", ProductiveOrientation.LIVESTOCK, "D", samplePolygon, null, 1.0, "");
        adapter.save(farm1);

        // Crear un polígono que se superpone parcialmente
        Polygon overlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(5, 5),
                new Coordinate(5, 15),
                new Coordinate(15, 15),
                new Coordinate(15, 5),
                new Coordinate(5, 5)
        });

        // Crear un polígono que no se superpone
        Polygon notOverlappingPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(20, 20),
                new Coordinate(20, 30),
                new Coordinate(30, 30),
                new Coordinate(30, 20),
                new Coordinate(20, 20)
        });

        // Act & Assert
        assertTrue(adapter.existsOverlappingFarm(overlappingPolygon, null), "Debería detectar superposición");
        assertFalse(adapter.existsOverlappingFarm(notOverlappingPolygon, null), "No debería detectar superposición");
        
        // Comprobar con UUID excluido (para la edición de la misma finca)
        assertFalse(adapter.existsOverlappingFarm(samplePolygon, farm1.getIdFarm()), "No debería chocar consigo misma");
    }
}
