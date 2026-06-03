import sys
import os
import subprocess
import rasterio
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import LinearSegmentedColormap
import urllib.request
import tempfile

temp_tif_file = None

def get_custom_cmap(map_type):
    """Crea y devuelve paletas de colores"""

    if map_type in ["NDVI", "GNDVI", "OSAVI"]:
        colors_with_positions = [
            (0.00, "#ffffff"),  # Blanco (Valor -1)
            (0.125, "#000000"),
            (0.375, "#ffffff"),  
            (0.52, "#000000"),  # Negro (Valor 0)
            (0.58, "#0867b4"),  # Celeste (Transición justo arriba del 0)
            (0.60, "#00ff00"),  # Verde
            (0.75, "#ffff00"),  # Amarillo (Valor 0.5)
            (0.88, "#ff0000"),  # Rojo
            (1.00, "#ff00ff")   # Magenta (Valor 1)
        ]

        # Creamos el mapa de colores
        cmap = LinearSegmentedColormap.from_list("custom_ndvi", colors_with_positions)
        return {'cmap': cmap, 'vmin': -1.0, 'vmax': 1.0}
    
    elif map_type in ["NDRE", "LCI"]:
        # Replicando la Imagen 2: Morado -> Azul -> Cian -> Verde -> Amarillo -> Rojo -> Rojo Oscuro
        colors = [
            (0.00, "#800080"), 
            (0.125, "#0000ff"), 
            (0.375, "#008cff"), 
            (0.50, "#00ff00"), 
            (0.57, "#ffff00"), 
            (0.90, "#ff0000"), 
            (1.00, "#800000")]
        cmap = LinearSegmentedColormap.from_list("custom_ndre", colors)
        return {'cmap': cmap, 'vmin': -1.0, 'vmax': 1.0}

    # Default fallback
    return {'cmap': plt.get_cmap('viridis'), 'vmin': 0.0, 'vmax': 1.0}

def apply_color_and_tile(tif_source, output_dir, map_type, gdal_script_path, is_url=False):
    """
    Procesa un archivo TIF y lo divide en paches/teselas.
    
    Args:
        tif_source: Ruta local del TIF o URL del TIF en MinIO
        output_dir: Directorio de salida para los paches
        map_type: Tipo de mapa espectral (NDVI, GNDVI, etc.)
        gdal_script_path: Ruta al script gdal2tiles.py
        is_url: Si True, tif_source es una URL y será descargada
    """
    print(f"Procesando {'URL' if is_url else 'archivo'}: {tif_source} como {map_type}")
    
    temp_download_path = None
    colored_tif = None
    try:
        # Si es URL, descargar el archivo
        if is_url:
            # mkstemp crea el archivo y devuelve un FileDescriptor (fd) y la ruta
            fd, temp_download_path = tempfile.mkstemp(suffix='.tif')
            # ¡CLAVE! Cerramos el FileDescriptor inmediatamente para liberar el archivo en Windows
            os.close(fd)
            try:
                print(f"Descargando TIF desde URL: {tif_source}")
                urllib.request.urlretrieve(tif_source, temp_download_path)
                input_tif = temp_download_path
            except Exception as e:
                print(f"Error descargando TIF desde URL: {e}")
                sys.exit(1)
        else:
            input_tif = tif_source

        colored_tif = os.path.join(os.path.dirname(input_tif), "colored.tif")

        if map_type != "RGB":
            # --- 1. APLICAR PSEUDOCOLOR PERSONALIZADO ---
            with rasterio.open(input_tif) as src:
                band = src.read(1)
                meta = src.meta.copy()

                # Obtener parámetros y paleta customizada
                params = get_custom_cmap(map_type)
                cmap = params['cmap']
                vmin, vmax = params['vmin'], params['vmax']

                # Crear máscara para ignorar los fondos reales del tif (bordes fuera del polígono, usualmente 0 o NoData)
                # Nota: Esto NO hace transparente los valores bajos del cultivo, solo los bordes del mapa.
                nodata_mask = band == 0

                # Normalizar los datos
                normalized = np.clip((band - vmin) / (vmax - vmin), 0, 1)

                # Aplicar mapa de colores (devuelve matriz RGBA de 4 bandas)
                colored_array = cmap(normalized)

                # Aplicar transparencia SOLO al fondo (fuera del mapa), no a los valores bajos
                colored_array[nodata_mask] = [0, 0, 0, 0]

                # Actualizar metadatos para guardar como RGBA de 8 bits
                meta.update(
                    dtype=rasterio.uint8,
                    count=4,
                    nodata=0
                )

                # Convertir de flotante (0-1) a entero (0-255)
                colored_uint8 = (colored_array * 255).astype('uint8')

                # Guardar el nuevo TIF temporal coloreado
                with rasterio.open(colored_tif, 'w', **meta) as dst:
                    for i in range(4): # R, G, B, Alpha
                        dst.write(colored_uint8[:, :, i], i + 1)

            target_tif = colored_tif
            print(f"Pseudocolor personalizado aplicado exitosamente.")
        else:
            # Si es RGB, usamos el original
            target_tif = input_tif

        # --- 2. CORTAR EN PACHES/TESELAS ---
        print("Generando paches (teselas XYZ)...")
        cores = max(1, os.cpu_count() - 1)

        # ---------------------------------------------------------
        # LÓGICA DINÁMICA DE EJECUCIÓN (PORTABILIDAD WINDOWS/AZURE)
        # ---------------------------------------------------------
        if "OSGeo4W" in gdal_script_path and os.name == 'nt':
            osgeo_base = gdal_script_path[:gdal_script_path.find("OSGeo4W") + 7]
            osgeo_bat = os.path.join(osgeo_base, "OSGeo4W.bat")

            print(f"Entorno OSGeo4W detectado. Usando: {osgeo_bat}")

            cmd_str = f'"{osgeo_bat}" python "{gdal_script_path}" --processes={cores} -z 12-22 -w none "{target_tif}" "{output_dir}"'

            result = subprocess.run(cmd_str, capture_output=True, text=True, shell=True)
        else:
            gdal2tiles_cmd = [
                "python", "-W", "ignore", gdal_script_path,
                f"--processes={cores}",
                "-z", "12-22",
                "-w", "none",
                target_tif,
                output_dir
            ]
            result = subprocess.run(gdal2tiles_cmd, capture_output=True, text=True)
        # ---------------------------------------------------------

        if result.returncode != 0:
            print("Error al cortar los paches:\n", result.stderr)
            sys.exit(1)

        print(f"Paches generados correctamente en: {output_dir}")
    
    finally:
        # Primero eliminamos el colored.tif si lo creamos
        if colored_tif and os.path.exists(colored_tif):
            try:
                os.remove(colored_tif)
                print("Archivo temporal colored.tif eliminado.")
            except Exception as e:
                print(f"Advertencia: No se pudo eliminar colored.tif: {e}")

        # Luego eliminamos el archivo descargado de la URL si existe
        if temp_download_path and os.path.exists(temp_download_path):
            try:
                os.remove(temp_download_path)
                print(f"Archivo temporal descargado eliminado: {temp_download_path}")
            except Exception as e:
                print(f"Advertencia: No se pudo eliminar temporal de URL: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 5:
        print("Uso: python MapProcessing.py <input_tif_o_url> <output_dir> <map_type> <gdal2tiles_path> [--url]")
        sys.exit(1)

    in_file = sys.argv[1]
    out_folder = sys.argv[2]
    m_type = sys.argv[3]
    gdal_path = sys.argv[4]

    
    # Verificar si se pasó el flag --url para indicar que in_file es una URL
    is_url_flag = len(sys.argv) > 5 and sys.argv[5] == "--url"

    apply_color_and_tile(in_file, out_folder, m_type, gdal_path, is_url=is_url_flag)
