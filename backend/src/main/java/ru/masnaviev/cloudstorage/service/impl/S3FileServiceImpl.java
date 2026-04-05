package ru.masnaviev.cloudstorage.service.impl;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseBuilder;
import ru.masnaviev.cloudstorage.exception.resource.*;
import ru.masnaviev.cloudstorage.repository.MinioRepository;
import ru.masnaviev.cloudstorage.service.S3FileService;
import ru.masnaviev.cloudstorage.util.Resource;
import ru.masnaviev.cloudstorage.util.ResourceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.*;
import static ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseBuilder.createDirectoryResponseFrom;
import static ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseBuilder.createFileResponseFrom;
import static ru.masnaviev.cloudstorage.util.ResourceBuilder.createFrom;
import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudstorage.util.ResourceType.FILE;
import static ru.masnaviev.cloudstorage.util.ZipBuilder.createZipFromResources;

@Component
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final MinioRepository repository;

    @Override
    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        StatObjectResponse response = repository.getResourceInfo(resourceData.getFullPath());

        return ResourceInfoResponseBuilder.createResponseFrom(resourceData.getFullPath(), resourceData.getResourceType() == DIRECTORY ? null : response.size());
    }

    @Override
    public void deleteResource(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        if (path.equals("/")) {
            throw new ParentDirectoryDeletionException(PROTECTED_PARENT_DIRECTORY);
        }

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        if (resourceData.getResourceType() == DIRECTORY) deleteDirectory(resourceData);
        else repository.deleteResource(resourceData.getFullPath());
    }

    @Override
    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files) {
        Resource resourceData = createFrom(userId, path);
        checkPathExists(resourceData.getPath());

        checkPathEndWithSlash(resourceData.getFullPath());

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            Resource fileData = createFrom(userId, path + file.getOriginalFilename());

            if (repository.checkResourceExists(fileData.getFullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            List<String> pathsToCreate = fileData.getPathsList();

            responses.addAll(uploadNonexistentDirectories(pathsToCreate).stream()
                    .map(ResourceInfoResponseBuilder::createDirectoryResponseFrom)
                    .toList());

            repository.uploadFile(fileData.getFullPath(), file);
            responses.add(createFileResponseFrom(fileData.getFullPath(), file.getSize()));
        }
        return responses;
    }

    @Override
    public DownloadResourceResponse downloadResource(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        InputStreamResource resource = resourceData.getResourceType() == DIRECTORY ? downloadDirectory(resourceData) : downloadFile(resourceData);

        return new DownloadResourceResponse(resourceData.getResourceName(), resource, resourceData.getResourceType());
    }

    @Override
    public ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo) {
        Resource pathFromData = createFrom(userId, pathFrom);
        checkPathExists(pathFromData.getPath());
        checkResourceExists(pathFromData.getFullPath(), pathFromData.getResourceType());

        Resource pathToData = createFrom(userId, pathTo);

        boolean isRename = !pathToData.getResourceName().equals(pathFromData.getResourceName());
        boolean isMoving = !pathToData.getPath().equals(pathFromData.getPath());

        if (isMoving == isRename) {
            throw new InvalidResourceOperationException(INVALID_OPERATION_COMBINATION);
        }

        checkPathExists(pathToData.getPath());

        if (pathToData.getResourceType() != pathFromData.getResourceType()) {
            throw new InvalidResourceTypeChangeException(INVALID_RESOURCE_TYPE_CHANGE);
        }

        ResourceInfoResponse response;
        if (pathFromData.getResourceType() == FILE) {
            moveResource(pathFromData.getFullPath(), pathToData.getFullPath());
            StatObjectResponse resourceInfo = repository.getResourceInfo(pathToData.getFullPath());
            response = createFileResponseFrom(pathToData.getFullPath(), resourceInfo.size());
        } else {
            Set<String> resourcesItemsByPrefix = repository.getResourcesItemsByPrefix(pathFromData.getFullPath(), true).keySet();
            for (String oldPath : resourcesItemsByPrefix) {
                String newPath = oldPath.replace(pathFromData.getFullPath(), pathToData.getFullPath());
                moveResource(oldPath, newPath);
            }
            response = createDirectoryResponseFrom(pathToData.getFullPath());
        }

        return response;
    }

    @Override
    public List<ResourceInfoResponse> searchResource(Long userId, String query) {
        Resource pathData = createFrom(userId, query);
        List<ResourceInfoResponse> resourcesByPrefix = getResourcesByPrefix(pathData.getUserFolder(), true);

        return resourcesByPrefix.stream().filter(r -> r.getType() != DIRECTORY).filter(r -> r.getName().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkResourceExists(resourceData.getPath(), resourceData.getResourceType());
        checkPathEndWithSlash(resourceData.getFullPath());

        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.getFullPath());
        return ResourceInfoResponseBuilder.createDirectoryResponseFrom(resourceData.getFullPath());
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), DIRECTORY);

        return getResourcesByPrefix(resourceData.getFullPath(), false);
    }

    @Override
    public Long createUserDirectory(Long userId) {
        String userFolder = "user-" + userId + "-files" + "/";
        repository.uploadDirectory(userFolder);
        return userId;
    }

    private void deleteDirectory(Resource resourceData) {
        Map<String, Item> resourcesItems = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), true);

        List<DeleteObject> objectsForDelete = resourcesItems.keySet().stream().map(DeleteObject::new).toList();

        repository.deleteResources(objectsForDelete);
    }

    private void checkPathExists(String path) {
        if (!repository.checkResourceExists(path)) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }
    }

    private void checkResourceExists(String path, ResourceType resourceType) {
        if (!repository.checkResourceExists(path)) {
            throw new ResourceNotFoundException(resourceType == DIRECTORY ? DIRECTORY_NOT_FOUND : FILE_NOT_FOUND);
        }
    }

    private void checkPathEndWithSlash(String path) {
        if (!path.endsWith("/")) {
            throw new PathMustEndWithSlashException(PATH_MUST_BE_END_SLASH);
        }
    }

    private InputStreamResource downloadDirectory(Resource resourceData) {
        Set<String> resourcesForDownload = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), true).keySet();

        Map<String, GetObjectResponse> downloadedResources = resourcesForDownload.stream().collect(Collectors.toMap(s -> s, repository::downloadResource));

        ByteArrayOutputStream byteOut = createZipFromResources(resourceData.getPath(), downloadedResources);

        return new InputStreamResource(new ByteArrayInputStream(byteOut.toByteArray()));
    }

    private InputStreamResource downloadFile(Resource resourceData) {
        return new InputStreamResource(repository.downloadResource(resourceData.getFullPath()));
    }

    private List<ResourceInfoResponse> getResourcesByPrefix(String resourcesPrefix, boolean recursively) {
        Map<String, Item> resultMap = repository.getResourcesItemsByPrefix(resourcesPrefix, recursively);
        resultMap.remove(resourcesPrefix);
        List<ResourceInfoResponse> responses = new ArrayList<>();
        for (Map.Entry<String, Item> result : resultMap.entrySet()) {
            responses.add(ResourceInfoResponseBuilder.createResponseFrom(result.getValue()));
        }
        return responses;
    }

    private List<String> uploadNonexistentDirectories(List<String> pathsToCreate) {
        Set<String> cachedDirectories = new HashSet<>();
        List<String> createdPaths = new ArrayList<>();
        for (String pathToCreate : pathsToCreate) {
            if (!cachedDirectories.contains(pathToCreate) && !repository.checkResourceExists(pathToCreate)) {
                repository.uploadDirectory(pathToCreate);
                createdPaths.add(pathToCreate);
                cachedDirectories.add(pathToCreate);
            }
        }
        return createdPaths;
    }

    private void moveResource(String from, String to) {
        repository.copyResource(from, to);
        repository.deleteResource(from);
    }
}