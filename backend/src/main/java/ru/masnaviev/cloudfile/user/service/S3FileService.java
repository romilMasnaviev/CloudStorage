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
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.user.exception.resource.DirectoryAlreadyExistsException;
import ru.masnaviev.cloudfile.user.exception.resource.PathMustEndWithSlashException;
import ru.masnaviev.cloudfile.user.exception.resource.PathNotFoundException;
import ru.masnaviev.cloudfile.user.exception.resource.ResourceNotFoundException;
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

        if (!checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }


        return getResourceInfo(resourceData);
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

        if (!checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (!checkResourceExists(resourceData.getFullPath()))
            throw new ResourceNotFoundException(resourceData.getResourceType() == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);

        if (resourceData.getResourceType() == DIRECTORY)
            deleteDirectory(resourceData);
        else
            deleteFile(resourceData);
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

        if (!checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (!checkResourceExists(resourceData.getFullPath()))
            throw new ResourceNotFoundException(resourceData.getResourceType() == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);

        return resourceData.getResourceType() == DIRECTORY ?
                downloadDirectory(resourceData) :
                downloadFile(resourceData);

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

        if (!checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PARENT_DIRECTORY_NOT_FOUND);
        }

        if (checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        createDirectory(resourceData.getFullPath());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndResourceName())
                .name(resourceData.getResourceName())
                .size(null)
                .resourceType(DIRECTORY)
                .build();
    }

    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .recursive(false)
                .maxKeys(100)
                .build());

        results.forEach(r -> {
        });

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (Result<Item> result : results) {
            if (result.get().objectName().equals(resourceData.getFullPath()))
                continue;

            responses.add(mapToResponse(userId, result.get().objectName(), result.get().size(), resourceData));
        }
        return responses;
    }

    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files)
            throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        // Проверяем, что путь до папки, куда будут загружаться ресурсы, существует
        var pathData = new NormalizedResourceData(userId, path);
        if (!checkResourceExists(pathData.getFullPath())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        // Проверка, что путь кончается на "/"
        if (!pathData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            NormalizedResourceData resourceData = new NormalizedResourceData(userId,
                    path + "/" + file.getOriginalFilename());

            // Получаем список папок для файла
            List<String> pathsToCreate = resourceData.getPathsList();

            // Рекурсивно получаем содержимое папки
            //TODO проверка что файл уже существует
            Iterable<Result<Item>> existedResources = client.listObjects(ListObjectsArgs.builder()
                    .bucket(minioBucketName)
                    .prefix(pathData.getFullPath())
                    .recursive(true)
                    .maxKeys(100)
                    .build());
            existedResources.forEach(r -> {
            });

            List<String> existedPaths = new ArrayList<>();

            for (Result<Item> resource : existedResources) {
                existedPaths.add(resource.get().objectName());
            }

            // Создаем папки при необходимости
            for (String pathToCreate : pathsToCreate) {
                if (!existedPaths.contains(pathToCreate)) {
                    createDirectory(pathToCreate);
                    var normalizedData = new NormalizedResourceData(userId, pathToCreate);
                    responses.add(mapToResponse(userId, pathToCreate, null, normalizedData));
                }
            }

            client.putObject(PutObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(resourceData.getFullPath())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());


            ResourceInfoResponse fileUploadResponse = mapToResponse(userId, resourceData.getFullPath(), file.getSize(), resourceData);

            responses.add(fileUploadResponse);
        }
        return responses;
    }


    private ResourceInfoResponse getResourceInfo(NormalizedResourceData resourceData) throws ServerException,
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
                .path(resourceData.getPathWithoutUsernameAndResourceName())
                .name(resourceData.getResourceName())
                .size(resourceData.getResourceType() == FILE ? response.size() : null)
                .resourceType(resourceData.getResourceType())
                .build();
    }

    private boolean checkResourceExists(String path) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(path)
                .recursive(false)
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


    private ResourceInfoResponse mapToResponse(Long userId, String objectName, Long size,
                                               NormalizedResourceData resourceData) {

        var resultData = new NormalizedResourceData(userId,
                objectName.replace(resourceData.getUserFolder(), ""));

        return ResourceInfoResponse.builder()
                .path(resultData.getPathWithoutUsernameAndResourceName())
                .name(resultData.getResourceName())
                .size(resultData.getResourceType() == DIRECTORY ? null : size)
                .resourceType(resultData.getResourceType())
                .build();
    }

    private InputStreamResource downloadFile(NormalizedResourceData resourceData) {
        // TODO
        return null;
    }

    public void createUserDirectory(Long userId) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        //TODO дублируется folder в NormalizedResourceData, подумать как отрефакторить
        //TODO сделать так чтобы корневую папку нельзя было удалить
        String userFolder = "user-" + userId + "-files" + "/";
        client.putObject(PutObjectArgs.builder()
                .bucket(minioBucketName)
                .object(userFolder)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    private ObjectWriteResponse createDirectory(String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        return client.putObject(PutObjectArgs.builder()
                .bucket(minioBucketName)
                .object(path)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

}