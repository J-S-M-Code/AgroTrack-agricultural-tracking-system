package com.agrotrack.infrastructure.adapters.out.storage;

import com.agrotrack.domain.port.out.storage.FileStoragePort;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import java.io.InputStream;

@Service
public class MinioStorageAdapter implements FileStoragePort {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String minioUrl;

    public MinioStorageAdapter(MinioClient minioClient,
                               @Value("${minio.bucket.name}") String bucketName,
                               @Value("${minio.url}") String minioUrl) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.minioUrl = minioUrl;
        initBucket(); // Comprobamos que el bucket exista al iniciar la app
    }

    private void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket '" + bucketName + "' creado exitosamente en MinIO.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando el bucket de MinIO", e);
        }
    }

    @Override
    public String uploadFile(String fileName, InputStream fileStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(fileStream, -1, 10485760) // Part size configurado a 10MB
                            .contentType(contentType)
                            .build()
            );

            // Retornamos la ruta completa para que se guarde como String en PostgreSQL
            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a MinIO: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo en MinIO: " + fileName, e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL pre-firmada para MinIO", e);
        }
    }

    @Override
    public void deleteDirectory(String prefix) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                deleteFile(item.objectName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el directorio en MinIO: " + prefix, e);
        }
    }
}