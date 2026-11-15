package com.agrotrack.infrastructure.adapters.out.storage;

import com.agrotrack.domain.port.out.storage.FileStoragePort;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.OffsetDateTime;

@Service
@Primary
public class AzureBlobStorageAdapter implements FileStoragePort {

    private final BlobContainerClient containerClient;

    public AzureBlobStorageAdapter(
            @Value("${azure.storage.connection-string}") String connectionString,
            @Value("${azure.storage.container-name}") String containerName) {
        
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Configurar CORS para evitar bloqueos del navegador en subidas directas
        try {
            com.azure.storage.blob.models.BlobServiceProperties properties = blobServiceClient.getProperties();
            java.util.List<com.azure.storage.blob.models.BlobCorsRule> corsRules = new java.util.ArrayList<>();
            com.azure.storage.blob.models.BlobCorsRule rule = new com.azure.storage.blob.models.BlobCorsRule()
                    .setAllowedOrigins("*") // En producción podés restringirlo a tu frontend
                    .setAllowedMethods("GET,PUT,POST,DELETE,OPTIONS,HEAD")
                    .setAllowedHeaders("*")
                    .setExposedHeaders("*")
                    .setMaxAgeInSeconds(3600);
            corsRules.add(rule);
            properties.setCors(corsRules);
            blobServiceClient.setProperties(properties);
            System.out.println("CORS rules applied successfully to Azure Blob Storage.");
        } catch (Exception e) {
            System.err.println("Could not apply CORS rules: " + e.getMessage());
        }
                
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        // Ensure container exists
        if (!containerClient.exists()) {
            containerClient.create();
            System.out.println("Container '" + containerName + "' created in Azure Blob Storage.");
        }
        
        // IMPORTANT: Set to Public Read (Blob) so that the Python script and Leaflet tiles
        // can be downloaded via their direct URL without needing a SAS token for every single tile.
        try {
            containerClient.setAccessPolicy(com.azure.storage.blob.models.PublicAccessType.BLOB, null);
        } catch (Exception e) {
            System.err.println("Could not set public access policy: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(String fileName, InputStream fileStream, long length, String contentType) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            
            // Usamos BlockBlobClient que tiene mejor soporte para streams y permite subir sin conocer el tamaño
            blobClient.getBlockBlobClient().upload(fileStream, length, true);
            blobClient.setHttpHeaders(headers);
            
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a Azure Blob Storage: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.deleteIfExists();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo en Azure Blob Storage: " + fileName, e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            
            BlobSasPermission permission = new BlobSasPermission()
                    .setReadPermission(true)
                    .setWritePermission(true)
                    .setCreatePermission(true);
            
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plusHours(2), permission);
                    
            String sasToken = blobClient.generateSas(values);
            return blobClient.getBlobUrl() + "?" + sasToken;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL pre-firmada para Azure", e);
        }
    }

    @Override
    public void deleteDirectory(String prefix) {
        try {
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix);
            for (BlobItem blobItem : containerClient.listBlobs(options, null)) {
                containerClient.getBlobClient(blobItem.getName()).deleteIfExists();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el directorio en Azure Blob Storage: " + prefix, e);
        }
    }
}
