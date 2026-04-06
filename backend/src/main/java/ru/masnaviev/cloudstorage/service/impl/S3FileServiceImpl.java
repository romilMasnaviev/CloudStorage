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
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponseAssembler;
import ru.masnaviev.cloudstorage.exception.resource.*;
import ru.masnaviev.cloudstorage.model.Resource;
import ru.masnaviev.cloudstorage.model.ResourceFactory;
import ru.masnaviev.cloudstorage.repository.MinioRepository;
import ru.masnaviev.cloudstorage.service.S3FileService;
import ru.masnaviev.cloudstorage.util.ResourceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.*;
import static ru.masnaviev.cloudstorage.model.ResourceFactory.createFromFullMinioPath;
import static ru.masnaviev.cloudstorage.model.ResourceFactory.createFromUserInput;
import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudstorage.util.ResourceType.FILE;
import static ru.masnaviev.cloudstorage.util.ZipBuilder.createZipFromResources;

@Component
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final MinioRepository repository;

    @Override
    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        StatObjectResponse response = repository.getResourceInfo(resourceData.getFullPath());
        return ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resourceData, response.size());
    }

    @Override
    public void deleteResource(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

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
        Resource resourceData = createFromUserInput(userId, path);
        checkPathExists(resourceData.getPath());

        checkPathEndWithSlash(resourceData.getFullPath());

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            Resource fileData = createFromUserInput(userId, path + file.getOriginalFilename());

            if (repository.checkResourceExists(fileData.getFullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            List<String> pathsToCreate = fileData.getPathsList();

            responses.addAll(uploadNonexistentDirectories(pathsToCreate, userId)
                    .stream()
                    .map(resource -> ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resource, null))
                    .toList());

            repository.uploadFile(fileData.getFullPath(), file);
            responses.add(ResourceInfoResponseAssembler.resourceToResourceInfoResponse(fileData, file.getSize()));
        }
        return responses;
    }

    @Override
    public DownloadResourceResponse downloadResource(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        InputStreamResource resource = resourceData.getResourceType() == DIRECTORY ? downloadDirectory(resourceData) : downloadFile(resourceData);

        return new DownloadResourceResponse(resourceData.getResourceName(), resource, resourceData.getResourceType());
    }

    @Override
    public ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo) {
        Resource pathFromData = createFromUserInput(userId, pathFrom);
        checkPathExists(pathFromData.getPath());
        checkResourceExists(pathFromData.getFullPath(), pathFromData.getResourceType());

        Resource pathToData = createFromUserInput(userId, pathTo);

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
            response = ResourceInfoResponseAssembler.resourceToResourceInfoResponse(pathToData, resourceInfo.size());
        } else {
            Set<String> resourcesItemsByPrefix = repository.getResourcesItemsByPrefix(pathFromData.getFullPath(), true).keySet();
            for (String oldPath : resourcesItemsByPrefix) {
                String newPath = oldPath.replace(pathFromData.getFullPath(), pathToData.getFullPath());
                moveResource(oldPath, newPath);
            }
            response = ResourceInfoResponseAssembler.resourceToResourceInfoResponse(pathToData, null);
        }

        return response;
    }

    @Override
    public List<ResourceInfoResponse> searchResource(Long userId, String query) {
        Resource pathData = createFromUserInput(userId, query);
        List<ResourceInfoResponse> resourcesByPrefix = getResourcesByPrefix(pathData.getUserFolder(), userId, true);

        return resourcesByPrefix.stream()
                .filter(r -> r.getType() != DIRECTORY)
                .filter(r -> r.getName().toLowerCase()
                        .contains(query.toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkResourceExists(resourceData.getPath(), resourceData.getResourceType());
        checkPathEndWithSlash(resourceData.getFullPath());

        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.getFullPath());
        return ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resourceData, null);
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.getPath());
        checkResourceExists(resourceData.getFullPath(), DIRECTORY);

        return getResourcesByPrefix(resourceData.getFullPath(), userId, false);
    }

    @Override
    public Long createUserDirectoryPath(Long userId) {
        String userFolder = ResourceFactory.getUserDirectoryPath(userId);
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

        Map<String, GetObjectResponse> downloadedResources = resourcesForDownload.stream()
                .collect(Collectors.toMap(s -> s, repository::downloadResource));

        ByteArrayOutputStream byteOut = createZipFromResources(resourceData.getPath(), downloadedResources);

        return new InputStreamResource(new ByteArrayInputStream(byteOut.toByteArray()));
    }

    private InputStreamResource downloadFile(Resource resourceData) {
        return new InputStreamResource(repository.downloadResource(resourceData.getFullPath()));
    }

    private List<ResourceInfoResponse> getResourcesByPrefix(String resourcesPrefix, Long userId, boolean recursively) {
        Map<String, Item> resultMap = repository.getResourcesItemsByPrefix(resourcesPrefix, recursively);
        resultMap.remove(resourcesPrefix);
        List<ResourceInfoResponse> responses = new ArrayList<>();
        for (Map.Entry<String, Item> result : resultMap.entrySet()) {
            Resource resource = createFromFullMinioPath(userId, result.getKey());
            responses.add(ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resource, result.getValue().size()));
        }
        return responses;
    }

    private List<Resource> uploadNonexistentDirectories(List<String> pathsToCreate, Long userId) {
        Set<String> cachedDirectories = new HashSet<>();
        List<Resource> createdResources = new ArrayList<>();
        for (String pathToCreate : pathsToCreate) {
            if (!cachedDirectories.contains(pathToCreate) && !repository.checkResourceExists(pathToCreate)) {
                repository.uploadDirectory(pathToCreate);
                createdResources.add(createFromFullMinioPath(userId, pathToCreate));
                cachedDirectories.add(pathToCreate);
            }
        }
        return createdResources;
    }

    private void moveResource(String from, String to) {
        repository.copyResource(from, to);
        repository.deleteResource(from);
    }
}