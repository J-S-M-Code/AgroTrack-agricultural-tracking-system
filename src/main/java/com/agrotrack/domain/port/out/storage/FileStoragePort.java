package com.agrotrack.domain.port.out.storage;

import java.io.InputStream;

public interface FileStoragePort {

    /**
     * Sube un archivo al almacenamiento y retorna la URL pública.
     *
     * @param fileName Nombre único del archivo (ej. UUID + extensión)
     * @param fileStream El flujo de datos del archivo
     * @param contentType El tipo de archivo (ej. "image/jpeg")
     * @return URL absoluta para acceder al archivo
     */
    String uploadFile(String fileName, InputStream fileStream, long length, String contentType);

    /**
     * Elimina un archivo del almacenamiento.
     */
    void deleteFile(String fileName);

    /**
     * Genera una URL pre-firmada para subir un archivo.
     * @param fileName Nombre del archivo.
     * @return URL pre-firmada.
     */
    String generatePresignedUploadUrl(String fileName);

    /**
     * Elimina todos los archivos con un prefijo especifico (como una carpeta).
     * @param prefix Prefijo o nombre de la carpeta.
     */
    void deleteDirectory(String prefix);
}