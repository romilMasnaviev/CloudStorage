package ru.masnaviev.cloudstorage.repository;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.exception.resource.MinioOperationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public void uploadDirectory(String path) {
        try {
            client.putObject(PutObjectArgs.builder()
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

    public Map<String, Item> getResourcesItemsByPrefix(String prefix, boolean recursively) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(prefix)
                .recursive(recursively)
                .build());

        results.forEach(r -> {
        });

        return toItemMapByPath(results);
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

    public void deleteResources(Iterable<DeleteObject> deleteObjects) {
        Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioBucketName)
                .objects(deleteObjects)
                .build());
        results.forEach(r -> {
        });
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

    public GetObjectResponse downloadResource(String fullPath) {
        try {
            return client.getObject(GetObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(fullPath)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
    }

    public void copyResource(String pathFrom, String pathTo) {
        try {
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(pathTo)
                    .source(CopySource.builder()
                            .bucket(minioBucketName)
                            .object(pathFrom)
                            .build())
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }

    }

    private Map<String, Item> toItemMapByPath(Iterable<Result<Item>> results) {
        Map<String, Item> paths = new HashMap<>();

        try {
            for (Result<Item> result : results) {
                paths.put(result.get().objectName(), result.get());
            }
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e.getMessage(), e.getCause());
        }
        return paths;
    }
}
