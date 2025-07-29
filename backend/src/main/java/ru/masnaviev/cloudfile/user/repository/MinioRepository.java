package ru.masnaviev.cloudfile.user.repository;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.exception.resource.MinioOperationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public ObjectWriteResponse uploadDirectory(String path) {
        try {
            return client.putObject(PutObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }

    public void uploadFile(String path, MultipartFile file) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }

    public StatObjectResponse getResourceInfo(String path) {
        try {
            return client.statObject(StatObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }

    public Iterable<Result<Item>> getResourcesByPrefix(String path, boolean recursively) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(path)
                .recursive(recursively)
                .build());
        results.forEach(r -> {
        });

        return results;
    }

    public void deleteResource(String path) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }

    public Iterable<Result<DeleteError>> deleteResources(Iterable<DeleteObject> deleteObjects) {
        Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioBucketName)
                .objects(deleteObjects)
                .build());
        results.forEach(r -> {
        });
        return results;
    }

    public boolean checkResourceExists(String path) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path)
                    .build());
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse().code().equals("NoSuchKey"))
                return false;
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
        return true;
    }

    public InputStreamResource downloadFile(String fullPath) {
        try {
            InputStream inputStream = client.getObject(GetObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(fullPath)
                    .build());
            return new InputStreamResource(inputStream);

        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }
}
