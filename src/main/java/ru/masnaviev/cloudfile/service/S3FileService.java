package ru.masnaviev.cloudfile.service;

import io.minio.GetObjectResponse;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.exception.resource.*;
import ru.masnaviev.cloudfile.repository.MinioRepository;
import ru.masnaviev.cloudfile.util.NormalizedResourceData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.util.NormalizedResourceData.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.NormalizedResourceData.ResourceType.FILE;
import static ru.masnaviev.cloudfile.util.ZipBuilder.createZipFromResources;

@Component
@RequiredArgsConstructor
public class S3FileService {

    private final MinioRepository repository;

    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName(), PATH_NOT_FOUND);

        return getResourceInfo(resourceData);
    }

    public void deleteResource(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName(), PATH_NOT_FOUND);
        checkResourceExists(resourceData);

        if (resourceData.getResourceType() == DIRECTORY)
            deleteDirectory(resourceData);
        else
            repository.deleteResource(resourceData.getFullPath());
    }

    public DownloadResourceResponse downloadResource(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName(), PATH_NOT_FOUND);
        checkResourceExists(resourceData);

        InputStreamResource resource = resourceData.getResourceType() == DIRECTORY ?
                downloadDirectory(resourceData) :
                downloadFile(resourceData);

        return new DownloadResourceResponse(resourceData.getResourceName(), resource, resourceData.getResourceType());
    }

    private InputStreamResource downloadDirectory(NormalizedResourceData resourceData) {
        Set<String> resourcesForDownload = toItemMapByPath(repository
                .getResourcesByPrefix(resourceData.getFullPath(), true)).keySet();

        Map<String, GetObjectResponse> downloadedResources = resourcesForDownload
                .stream()
                .collect(Collectors.toMap(s -> s, repository::downloadResource));

        ByteArrayOutputStream byteOut = createZipFromResources(resourceData.getPathWithoutResourceName(), downloadedResources);

        return new InputStreamResource(new ByteArrayInputStream(byteOut.toByteArray()));
    }

    private InputStreamResource downloadFile(NormalizedResourceData resourceData) {
        return new InputStreamResource(repository.downloadResource(resourceData.getFullPath()));
    }

    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName(), PATH_NOT_FOUND);

        if (!resourceData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }
        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.getFullPath());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndResourceName())
                .name(resourceData.getResourceName())
                .size(null)
                .resourceType(DIRECTORY)
                .build();
    }

    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName(), PATH_NOT_FOUND);

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
            Set<String> existedPaths = toItemMapByPath(existedResources).keySet();

            pathsToCreate.removeAll(existedPaths);

            // Создаем папки при необходимости
            for (String pathToCreate : pathsToCreate) {
                repository.uploadDirectory(pathToCreate);
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

        Set<String> pathsForDelete = toItemMapByPath(resourcesForDelete).keySet();

        List<DeleteObject> objectsForDelete = pathsForDelete.stream().map(DeleteObject::new).toList();

        repository.deleteResources(objectsForDelete);
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
        repository.uploadDirectory(userFolder);
    }

    private void checkPathExists(String path, String errorMessage) {
        if (!repository.checkResourceExists(path)) {
            throw new PathNotFoundException(errorMessage);
        }
    }

    private void checkResourceExists(NormalizedResourceData resourceData) {
        if (!repository.checkResourceExists(resourceData.getFullPath())) {
            throw new ResourceNotFoundException(resourceData.getResourceType() == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);
        }
    }
}