package com.agrotrack.infrastructure.adapters.out.lot;

import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.out.lot.MapTilingPort;
import com.agrotrack.domain.port.out.storage.FileStoragePort;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;

import org.geotools.api.filter.FilterFactory;
import org.geotools.api.style.*;
import org.geotools.factory.CommonFactoryFinder;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class MapTilingAdapter implements MapTilingPort {

    private final FileStoragePort fileStoragePort;
    private final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    public MapTilingAdapter(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    @Override
    public void processAndStoreTiles(InputStream geoTiffStream, String mapId, SpectralMapType mapType) {
        System.out.println("Iniciando procesamiento de mapa (" + mapType + "): " + mapId);

        try {
            GeoTiffReader reader = new GeoTiffReader(geoTiffStream);
            GridCoverage2D coverage = reader.read(null);

            ReferencedEnvelope envelope = new ReferencedEnvelope(coverage.getEnvelope2D());
            double minLon = envelope.getMinX();
            double minLat = envelope.getMinY();
            double maxLon = envelope.getMaxX();
            double maxLat = envelope.getMaxY();

            MapContent mapContent = new MapContent();

            //Elegimos el estilo basado en el tipo de mapa
            Style style = createStyleForMapType(mapType);

            GridCoverageLayer layer = new GridCoverageLayer(coverage, style);
            mapContent.addLayer(layer);

            StreamingRenderer renderer = new StreamingRenderer();
            renderer.setMapContent(mapContent);

            int minZoom = 8;
            int maxZoom = 20;

            for (int z = minZoom; z <= maxZoom; z++) {
                int minX = lon2tiley(minLon, z);
                int maxX = lon2tiley(maxLon, z);
                int minY = lat2tiley(maxLat, z);
                int maxY = lat2tiley(minLat, z);

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {

                        BufferedImage tileImage = renderTile(renderer, x, y, z);

                        if (tileImage != null) {
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            ImageIO.write(tileImage, "png", os);
                            InputStream tileStream = new ByteArrayInputStream(os.toByteArray());

                            String fileName = "mapas-espectrales/" + mapId + "/" + z + "/" + x + "/" + y + ".png";
                            fileStoragePort.uploadFile(fileName, tileStream, "image/png");
                        }
                    }
                }
                System.out.println("Zoom " + z + " completado.");
            }

            mapContent.dispose();
            System.out.println("Procesamiento completado para: " + mapId);

        } catch (Exception e) {
            throw new RuntimeException("Error crítico procesando el mapa", e);
        }
    }

    /**
     * Decide qué estilo aplicar.
     */
    private Style createStyleForMapType(SpectralMapType type) {
        if (type == SpectralMapType.RGB) {
            return createRGBStyle();
        } else {
            // NDVI, GNDVI, OSAVI, LCI, NDRE comparten la lógica de paleta térmica de vegetación.
            // Si en el futuro quieres colores distintos por índice, puedes separarlos en más métodos.
            return createVegetationIndexStyle();
        }
    }

    /**
     * Estilo para RGB: Le dice a GeoTools que mapee las bandas del TIFF a los colores reales.
     */
    private Style createRGBStyle() {
        RasterSymbolizer sym = sf.createRasterSymbolizer();

        // Mapear Banda 1 a Rojo, Banda 2 a Verde, Banda 3 a Azul
        SelectedChannelType redChannel = sf.createSelectedChannelType("1", null);
        SelectedChannelType greenChannel = sf.createSelectedChannelType("2", null);
        SelectedChannelType blueChannel = sf.createSelectedChannelType("3", null);

        ChannelSelection channelSelection = sf.createChannelSelection(
                new SelectedChannelType[]{redChannel, greenChannel, blueChannel}
        );

        sym.setChannelSelection(channelSelection);

        return wrapInStyle(sym);
    }

    /**
     * Estilo para Índices de Vegetación (Una sola banda matemática).
     */
    private Style createVegetationIndexStyle() {
        ColorMap colorMap = sf.createColorMap();

        // Rojo (Suelo desnudo, agua, o planta muerta)
        ColorMapEntry entry1 = sf.createColorMapEntry();
        entry1.setQuantity(ff.literal(-1.0));
        entry1.setColor(ff.literal("#d7191c"));

        // Naranja/Amarillo (Planta bajo estrés)
        ColorMapEntry entry2 = sf.createColorMapEntry();
        entry2.setQuantity(ff.literal(0.1));
        entry2.setColor(ff.literal("#fdae61"));

        // Verde claro (Vegetación sana)
        ColorMapEntry entry3 = sf.createColorMapEntry();
        entry3.setQuantity(ff.literal(0.5));
        entry3.setColor(ff.literal("#a6d96a"));

        // Verde oscuro (Vegetación muy densa y vigorosa)
        ColorMapEntry entry4 = sf.createColorMapEntry();
        entry4.setQuantity(ff.literal(1.0));
        entry4.setColor(ff.literal("#1a9641"));

        colorMap.addColorMapEntry(entry1);
        colorMap.addColorMapEntry(entry2);
        colorMap.addColorMapEntry(entry3);
        colorMap.addColorMapEntry(entry4);

        RasterSymbolizer sym = sf.createRasterSymbolizer();
        sym.setColorMap(colorMap);

        return wrapInStyle(sym);
    }

    /**
     * Método auxiliar para envolver el Simbolizador en las reglas y estilos de GeoTools (Mamushkas)
     */
    private Style wrapInStyle(RasterSymbolizer sym) {
        Rule rule = sf.createRule();
        rule.symbolizers().add(sym);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    // ... (El método renderTile, lon2tiley y lat2tiley quedan exactamente igual que antes) ...
    private BufferedImage renderTile(StreamingRenderer renderer, int x, int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        double tileMaxLat = Math.toDegrees(Math.atan(Math.sinh(n)));
        double tileMinLon = x / Math.pow(2.0, z) * 360.0 - 180.0;

        double n2 = Math.PI - (2.0 * Math.PI * (y + 1)) / Math.pow(2.0, z);
        double tileMinLat = Math.toDegrees(Math.atan(Math.sinh(n2)));
        double tileMaxLon = (x + 1) / Math.pow(2.0, z) * 360.0 - 180.0;

        ReferencedEnvelope tileEnvelope = new ReferencedEnvelope(
                tileMinLon, tileMaxLon, tileMinLat, tileMaxLat,
                org.geotools.referencing.crs.DefaultGeographicCRS.WGS84
        );

        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, 256, 256);
        graphics.setComposite(AlphaComposite.SrcOver);

        Rectangle paintArea = new Rectangle(0, 0, 256, 256);
        renderer.paint(graphics, paintArea, tileEnvelope);
        graphics.dispose();

        return image;
    }

    private int lon2tiley(double lon, int z) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << z));
    }

    private int lat2tiley(double lat, int z) {
        return (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << z));
    }
}