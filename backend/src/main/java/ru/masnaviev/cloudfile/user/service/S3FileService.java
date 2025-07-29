package ru.masnaviev.cloudfile.user.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.user.exception.resource.*;
import ru.masnaviev.cloudfile.user.repository.MinioRepository;
import ru.masnaviev.cloudfile.user.util.NormalizedResourceData;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.FILE;

@Component
@RequiredArgsConstructor
public class S3FileService {

    private final MinioRepository repository;

    public ResourceInfoResponse getResourceInfo(Long userId, String path) {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!repository.checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        return getResourceInfo(resourceData);
    }

    public void deleteResource(Long userId, String path) {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!repository.checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (!repository.checkResourceExists(resourceData.getFullPath()))
            throw new ResourceNotFoundException(resourceData.getResourceType() == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);

        if (resourceData.getResourceType() == DIRECTORY)
            deleteDirectory(resourceData);
        else
            repository.deleteResource(resourceData.getFullPath());
    }

    public InputStreamResource downloadResource(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!repository.checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        if (!repository.checkResourceExists(resourceData.getFullPath()))
            throw new ResourceNotFoundException(resourceData.getResourceType() == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);

        return resourceData.getResourceType() == DIRECTORY ?
                downloadDirectory(resourceData) :
                downloadFile(resourceData);

    }

    public ResourceInfoResponse uploadDirectory(Long userId, String path) {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!resourceData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }

        if (!repository.checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PARENT_DIRECTORY_NOT_FOUND);
        }

        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.createDirectory(resourceData.getFullPath());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndResourceName())
                .name(resourceData.getResourceName())
                .size(null)
                .resourceType(DIRECTORY)
                .build();
    }

    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        if (!repository.checkResourceExists(resourceData.getPathWithoutResourceName())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        Iterable<Result<Item>> results = repository.getResourcesByPrefix(resourceData.getFullPath(), false);

        List<ResourceInfoResponse> responses = new ArrayList<>();

        Map<String, Item> resultMap = toItemMapByPath(results);

        for (Map.Entry<String, Item> result : resultMap.entrySet()) {

            if (result.getKey().equals(resourceData.getFullPath()))
                responses.add(toResponse(userId, result.getKey(), result.getValue().size(), resourceData));

        }
        return responses;
    }

    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files) {

        // Проверяем, что путь до папки, куда будут загружаться ресурсы, существует
        var pathData = new NormalizedResourceData(userId, path);
        if (!repository.checkResourceExists(pathData.getFullPath())) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }

        // Проверка, что путь кончается на "/"
        if (!pathData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            NormalizedResourceData resourceData = new NormalizedResourceData(userId, path + "/" + file.getOriginalFilename());

            if (repository.checkResourceExists(resourceData.getFullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            // Получаем список папок для файла
            List<String> pathsToCreate = resourceData.getPathsList();

            // Рекурсивно получаем содержимое папки
            Iterable<Result<Item>> existedResources = repository.getResourcesByPrefix(pathData.getFullPath(), true);

            // Получаем список существующих ресурсов
            Map<String, Item> existedPaths = toItemMapByPath(existedResources);

            pathsToCreate.removeAll(existedPaths.keySet());

            // Создаем папки при необходимости
            for (String pathToCreate : pathsToCreate) {
                repository.createDirectory(pathToCreate);
                var normalizedData = new NormalizedResourceData(userId, pathToCreate);
                responses.add(toResponse(userId, pathToCreate, null, normalizedData));
            }

            repository.uploadFile(resourceData.getFullPath(), file);

            ResourceInfoResponse fileUploadResponse = toResponse(userId, resourceData.getFullPath(), file.getSize(), resourceData);

            responses.add(fileUploadResponse);
        }
        return responses;
    }


    private ResourceInfoResponse getResourceInfo(NormalizedResourceData resourceData) {

        StatObjectResponse response = repository.getResourceInfo(resourceData.getFullPath());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndResourceName())
                .name(resourceData.getResourceName())
                .size(resourceData.getResourceType() == FILE ? response.size() : null)
                .resourceType(resourceData.getResourceType())
                .build();
    }

    private void deleteDirectory(NormalizedResourceData resourceData) {

        Iterable<Result<Item>> resourcesForDelete = repository.getResourcesByPrefix(resourceData.getFullPath(), true);

        Map<String, Item> pathsForDelete = toItemMapByPath(resourcesForDelete);

        List<DeleteObject> objectsForDelete = pathsForDelete.keySet().stream().map(DeleteObject::new).toList();

        repository.deleteResources(objectsForDelete);
    }

    private InputStreamResource downloadDirectory(NormalizedResourceData resourceData) {

        // TODO на данный момент нельзя скачать папку так как я ее не сам создаю. После реализации метода
        // TODO по созданию пустой папки можно доделать до конца. Также нужно реализовать downloadFile
//        InputStream stream = repository.downloadDirectory(resourceData.getFullPath());
//        return new InputStreamResource(stream);
        return null;
    }


    private ResourceInfoResponse toResponse(Long userId, String objectName, Long size,
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

    public void createUserDirectory(Long userId) {

        //TODO дублируется folder в NormalizedResourceData, подумать как отрефакторить
        //TODO сделать так чтобы корневую папку нельзя было удалить
        String userFolder = "user-" + userId + "-files" + "/";
        repository.createDirectory(userFolder);
    }

}