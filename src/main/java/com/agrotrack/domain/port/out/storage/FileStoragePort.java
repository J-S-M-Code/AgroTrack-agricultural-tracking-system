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
    String uploadFile(String fileName, InputStream fileStream, String contentType);

    /**
     * Elimina un archivo del almacenamiento.
     */
    void deleteFile(String fileName);
}