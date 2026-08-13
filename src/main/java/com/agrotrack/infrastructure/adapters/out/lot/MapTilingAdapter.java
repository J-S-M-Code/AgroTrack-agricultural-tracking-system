package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.lot.MapTilingPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class MapTilingAdapter implements MapTilingPort {

    private final FileStoragePort fileStoragePort;

    @Value("${python.env.path:python}")
    private String pythonExec;

    @Value("${python.script.mappaches:scripts/MapProcessing.py}")
    private String mapPachesScript;

    // Lee la ruta desde el application.properties o variables de entorno
    @Value("${gdal.script.path:C:/Users/Joaco/AppData/Local/Programs/OSGeo4W/apps/gdal-dev/Scripts/gdal2tiles.py}")
    private String gdal2TilesScript;

    public MapTilingAdapter(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public void processAndStoreTiles(String tifUrl, UUID mapId, UUID idFamr, SpectralMapType mapType, String flightDateStr) {
        System.out.println("⚡ Iniciando motor Python (MapPaches) para: " + mapId);
        System.out.println("📥 URL del TIF: " + tifUrl);
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("agrotrack_map_" + mapId);
            Path tilesDir = tempDir.resolve("tiles_out");
            Files.createDirectories(tilesDir);

            // Pasamos la URL del TIF al script Python con el flag --url
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExec,
                    mapPachesScript,
                    tifUrl,
                    tilesDir.toAbsolutePath().toString(),
                    mapType.name(),
                    gdal2TilesScript,
                    "--url"
            );

            pb.inheritIO();
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("El script MapProcessing.py falló con código: " + exitCode);
            }

            System.out.println("Subiendo paches a MinIO...");
            uploadTilesToMinio(tilesDir, mapId, idFamr, flightDateStr);

        } catch (Exception e) {
            throw new RuntimeException("Error crítico procesando mapa con Python: " + e.getMessage(), e);
        } finally {
            if (tempDir != null) deleteDirectoryRecursively(tempDir);
        }
    }

    private void uploadTilesToMinio(Path tilesDir, UUID mapId, UUID idFamr, String flightDateStr) throws IOException {
        try (Stream<Path> paths = Files.walk(tilesDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".png"))
                    .parallel()
                    .forEach(path -> {
                        String relativePath = tilesDir.relativize(path).toString().replace("\\", "/");
                        String minioFileName = "mapas-espectrales/procesados/" + idFamr + "/" + flightDateStr + "/" + mapId + "/" + relativePath;

                        try (InputStream is = Files.newInputStream(path)) {
                            fileStoragePort.uploadFile(minioFileName, is, "image/png");
                        } catch (IOException e) {
                            System.err.println("Error subiendo a MinIO: " + relativePath);
                        }
                    });
        }
    }

    private void deleteDirectoryRecursively(Path pathToBeDeleted) {
        try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("No se pudo eliminar la carpeta temporal.");
        }
    }
}