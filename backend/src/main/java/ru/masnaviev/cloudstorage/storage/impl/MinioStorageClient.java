package ru.masnaviev.cloudstorage.storage.impl;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.config.minio.MinioProperties;
import ru.masnaviev.cloudstorage.exception.resource.MinioOperationException;
import ru.masnaviev.cloudstorage.exception.resource.ResourceNotFoundException;
import ru.masnaviev.cloudstorage.storage.StorageClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MinioStorageClient implements StorageClient {

    private final MinioClient client;
    private final MinioProperties minioProperties;

    @Override
    public void uploadDirectory(String path) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void uploadFile(String path, MultipartFile file) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public StatObjectResponse getResourceInfo(String path) {
        try {
            return client.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Resource not found");
            }
            throw new MinioOperationException(e);
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public Map<String, Item> getResourcesItemsByPrefix(String prefix, boolean recursively) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperties.bucketName())
                .prefix(prefix)
                .recursive(recursively)
                .build());
        return toItemMapByPath(results);
    }

    @Override
    public void deleteResource(String path) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void deleteResources(Iterable<DeleteObject> deleteObjects) {
        Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioProperties.bucketName())
                .objects(deleteObjects)
                .build());
        results.forEach(r -> {
        });
    }

    @Override
    public boolean checkResourceExists(String path) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path)
                    .build());
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse().code().equals("NoSuchKey"))
                return false;
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
        return true;
    }

    @Override
    public GetObjectResponse downloadResource(String fullPath) {
        try {
            return client.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(fullPath)
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
    }

    @Override
    public void copyResource(String pathFrom, String pathTo) {
        try {
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(pathTo)
                    .source(CopySource.builder()
                            .bucket(minioProperties.bucketName())
                            .object(pathFrom)
                            .build())
                    .build());
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }

    }

    private Map<String, Item> toItemMapByPath(Iterable<Result<Item>> results) {
        Map<String, Item> paths = new HashMap<>();

        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                paths.put(item.objectName(), item);
            }
        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new MinioOperationException(e);
        }
        return paths;
    }
}
