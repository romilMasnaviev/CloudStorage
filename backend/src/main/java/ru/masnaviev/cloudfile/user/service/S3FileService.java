package ru.masnaviev.cloudfile.user.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudfile.user.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.user.exception.resource.*;
import ru.masnaviev.cloudfile.user.util.NormalizedResourceData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.FILE;

@Component
@RequiredArgsConstructor
public class S3FileService {

    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public ResourceInfoResponse getResourceInfo(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!checkPathExists(resourceData)) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        return resourceData.getResourceType() == DIRECTORY ?
                getDirectoryInfo(resourceData) :
                getFileInfo(resourceData);
    }

    public void deleteResource(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!checkPathExists(resourceData)) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (resourceData.getResourceType() == DIRECTORY) {
            if (!checkDirectoryExists(resourceData)) {
                throw new DirectoryNotFoundException(DIRECTORY_NOT_FOUND);
            }
            deleteDirectory(resourceData);
        } else {
            if (!checkFileExists(resourceData)) {
                throw new FileNotFoundException(FILE_NOT_FOUND);
            }
            deleteFile(resourceData);
        }
    }

    public InputStreamResource downloadResource(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!checkPathExists(resourceData)) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (resourceData.getResourceType() == DIRECTORY) {
            if (!checkDirectoryExists(resourceData))
                throw new DirectoryNotFoundException(DIRECTORY_NOT_FOUND);

            return downloadDirectory(resourceData);
        } else {
            if (!checkFileExists(resourceData))
                throw new FileNotFoundException(FILE_NOT_FOUND);

            return downloadFile(resourceData);
        }
    }

    public ResourceInfoResponse uploadDirectory(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!resourceData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }

        if (!checkPathExists(resourceData)) {
            throw new PathNotFoundException(PARENT_DIRECTORY_NOT_FOUND);
        }

        if (checkDirectoryExists(resourceData)) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        client.putObject(PutObjectArgs.builder()
                .bucket(minioBucketName)
                .object(resourceData.getFullPath())
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndFilename())
                .name(resourceData.getResourceName())
                .size(null)
                .resourceType(DIRECTORY)
                .build();
    }


    private ResourceInfoResponse getDirectoryInfo(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());

        results.forEach(r -> {
        });

        if (results.iterator().hasNext()) {
            return ResourceInfoResponse.builder()
                    .path(resourceData.getPathWithoutUsernameAndFilename())
                    .name(resourceData.getResourceName())
                    .size(null)
                    .resourceType(DIRECTORY)
                    .build();
        } else {
            throw new ResourceNotFoundException(RESOURCE_NOT_FOUND);
        }
    }

    private ResourceInfoResponse getFileInfo(NormalizedResourceData resourceData) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        StatObjectResponse response = client.statObject(StatObjectArgs.builder()
                .bucket(minioBucketName)
                .object(resourceData.getFullPath())
                .build());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndFilename())
                .name(resourceData.getResourceName())
                .size(response.size())
                .resourceType(FILE)
                .build();
    }

    private boolean checkDirectoryExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        return results.iterator().hasNext();
    }

    private boolean checkFileExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        return results.iterator().hasNext();
    }

    private boolean checkPathExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getPathWithoutFilename())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        return results.iterator().hasNext();
    }


    private void deleteFile(NormalizedResourceData resourceData) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        client.removeObject(RemoveObjectArgs.builder()
                .bucket(minioBucketName)
                .object(resourceData.getFullPath())
                .build());
    }

    private void deleteDirectory(NormalizedResourceData resourceData) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        List<DeleteObject> deleteObjects = new ArrayList<>();

        Iterable<Result<Item>> resourcesForDelete = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .recursive(true)
                .build());


        for (Result<Item> result : resourcesForDelete) {
            deleteObjects.add(new DeleteObject(result.get().objectName()));
        }

        Iterable<Result<DeleteError>> deletedResources = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioBucketName)
                .objects(deleteObjects)
                .build());

        deletedResources.forEach(r -> {
        });
    }

    private InputStreamResource downloadDirectory(NormalizedResourceData resourceData) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        // TODO на данный момент нельзя скачать папку так как я ее не сам создаю. После реализации метода
        // TODO по созданию пустой папки можно доделать до конца. Также нужно реализовать downloadFile
        InputStream stream = client.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(resourceData.getFullPath())
                        .build());
        return new InputStreamResource(stream);
    }

    private InputStreamResource downloadFile(NormalizedResourceData resourceData) {
        // TODO
        return null;
    }
}

