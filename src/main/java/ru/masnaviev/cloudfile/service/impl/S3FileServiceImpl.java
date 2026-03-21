package ru.masnaviev.cloudfile.service.impl;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
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
import ru.masnaviev.cloudfile.service.S3FileService;
import ru.masnaviev.cloudfile.util.Resource;
import ru.masnaviev.cloudfile.util.ResourceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.util.ResourceBuilder.createFrom;
import static ru.masnaviev.cloudfile.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.ResourceType.FILE;
import static ru.masnaviev.cloudfile.util.ZipBuilder.createZipFromResources;

@Component
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final MinioRepository repository;

    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());


        StatObjectResponse response = repository.getResourceInfo(resourceData.getFullPath());

        return buildResponse(resourceData.getPathWithoutUsernameAndResourceName(), resourceData.getResourceName(),
                resourceData.getResourceType() == FILE ? response.size() : null, resourceData.getResourceType());
    }

    public void deleteResource(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        if (path.equals("/")) {
            throw new ParentDirectoryDeletionException(PROTECTED_PARENT_DIRECTORY);
        }

        checkPathExists(resourceData.getPathWithoutResourceName());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        if (resourceData.getResourceType() == DIRECTORY)
            deleteDirectory(resourceData);
        else
            repository.deleteResource(resourceData.getFullPath());
    }

    public DownloadResourceResponse downloadResource(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        InputStreamResource resource = resourceData.getResourceType() == DIRECTORY ?
                downloadDirectory(resourceData) :
                downloadFile(resourceData);

        return new DownloadResourceResponse(resourceData.getResourceName(), resource, resourceData.getResourceType());
    }

    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkResourceExists(resourceData.getPathWithoutResourceName(), resourceData.getResourceType());
        checkPathEndWithSlash(resourceData);

        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.getFullPath());

        return buildResponse(resourceData.getPathWithoutUsernameAndResourceName(), resourceData.getResourceName(), null, DIRECTORY);
    }

    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());

        Map<String, Item> resultMap = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), false);

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (Map.Entry<String, Item> result : resultMap.entrySet()) {

            var resultData = createFrom(userId,
                    result.getKey().replace(resourceData.getUserFolder(), ""));

            ResourceInfoResponse response = buildResponse(resultData.getPathWithoutUsernameAndResourceName(), resultData.getResourceName(),
                    resultData.getResourceType() == DIRECTORY ? null : result.getValue().size(), resultData.getResourceType());

            responses.add(response);

        }
        return responses;
    }

    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files) {
        Resource pathData = createFrom(userId, path);
        checkPathExists(pathData.getFullPath());

        checkPathEndWithSlash(pathData);

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            Resource resourceData = createFrom(userId, path + file.getOriginalFilename());

            if (repository.checkResourceExists(resourceData.getFullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            List<String> pathsToCreate = resourceData.getPathsList();

            Set<String> existedPaths = repository.getResourcesItemsByPrefix(pathData.getFullPath(), true).keySet();

            pathsToCreate.removeAll(existedPaths);

            for (String pathToCreate : pathsToCreate) {
                repository.uploadDirectory(pathToCreate);
                var normalizedData = createFrom(userId, pathToCreate);
                ResourceInfoResponse response = buildResponse(normalizedData.getPathWithoutUsernameAndResourceName(), normalizedData.getResourceName(), null, normalizedData.getResourceType());

                responses.add(response);
            }

            repository.uploadFile(resourceData.getFullPath(), file);
            ResourceInfoResponse response = buildResponse(resourceData.getPathWithoutUsernameAndResourceName(), resourceData.getResourceName(), file.getSize(), resourceData.getResourceType());

            responses.add(response);
        }
        return responses;
    }

    public void createUserDirectory(Long userId) {
        String userFolder = "user-" + userId + "-files" + "/";
        repository.uploadDirectory(userFolder);
    }

    private void deleteDirectory(Resource resourceData) {

        Map<String, Item> ResourcesItems = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), true);

        List<DeleteObject> objectsForDelete = ResourcesItems.keySet().stream().map(DeleteObject::new).toList();

        repository.deleteResources(objectsForDelete);
    }

    private void checkPathExists(String path) {
        if (!repository.checkResourceExists(path)) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }
    }

    private void checkResourceExists(String path, ResourceType resourceType) {
        if (!repository.checkResourceExists(path)) {
            throw new ResourceNotFoundException(resourceType == DIRECTORY ?
                    DIRECTORY_NOT_FOUND :
                    FILE_NOT_FOUND);
        }
    }

    private void checkPathEndWithSlash(Resource pathData) {
        if (!pathData.getFullPath().endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }
    }

    private ResourceInfoResponse buildResponse(String path, String name, Long size, ResourceType resourceType) {
        return ResourceInfoResponse.builder()
                .path(path)
                .name(name)
                .size(size)
                .resourceType(resourceType)
                .build();
    }

    private InputStreamResource downloadDirectory(Resource resourceData) {
        Set<String> resourcesForDownload = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), true).keySet();

        Map<String, GetObjectResponse> downloadedResources = resourcesForDownload
                .stream()
                .collect(Collectors.toMap(s -> s, repository::downloadResource));

        ByteArrayOutputStream byteOut = createZipFromResources(resourceData.getPathWithoutResourceName(), downloadedResources);

        return new InputStreamResource(new ByteArrayInputStream(byteOut.toByteArray()));
    }

    private InputStreamResource downloadFile(Resource resourceData) {
        return new InputStreamResource(repository.downloadResource(resourceData.getFullPath()));
    }
}