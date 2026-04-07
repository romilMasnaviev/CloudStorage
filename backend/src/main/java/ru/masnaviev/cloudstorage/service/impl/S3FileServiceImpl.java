package ru.masnaviev.cloudstorage.service.impl;

import io.minio.StatObjectResponse;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.assembler.ResourceInfoResponseAssembler;
import ru.masnaviev.cloudstorage.download.ResourceDownloadData;
import ru.masnaviev.cloudstorage.download.ResourceWriter;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.exception.resource.*;
import ru.masnaviev.cloudstorage.model.Resource;
import ru.masnaviev.cloudstorage.model.ResourceFactory;
import ru.masnaviev.cloudstorage.model.ResourceType;
import ru.masnaviev.cloudstorage.service.S3FileService;
import ru.masnaviev.cloudstorage.storage.StorageClient;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.*;
import static ru.masnaviev.cloudstorage.model.ResourceFactory.createFromFullMinioPath;
import static ru.masnaviev.cloudstorage.model.ResourceFactory.createFromUserInput;
import static ru.masnaviev.cloudstorage.model.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudstorage.model.ResourceType.FILE;

@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {

    private final StorageClient repository;

    @Override
    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.path());
        checkResourceExists(resourceData.fullPath(), resourceData.resourceType());

        StatObjectResponse response = repository.getResourceInfo(resourceData.fullPath());
        return ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resourceData, response.size());
    }

    @Override
    public void deleteResource(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        if (path.equals("/")) {
            throw new ParentDirectoryDeletionException(PROTECTED_PARENT_DIRECTORY);
        }

        checkPathExists(resourceData.path());
        checkResourceExists(resourceData.fullPath(), resourceData.resourceType());

        if (resourceData.resourceType() == DIRECTORY) deleteDirectory(resourceData);
        else repository.deleteResource(resourceData.fullPath());
    }

    @Override
    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files) {
        Resource resourceData = createFromUserInput(userId, path);
        checkPathExists(resourceData.path());

        checkPathEndWithSlash(resourceData.fullPath());

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            Resource fileData = createFromUserInput(userId, path + file.getOriginalFilename());

            if (repository.checkResourceExists(fileData.fullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            List<String> pathsToCreate = fileData.getPathsList();

            responses.addAll(uploadNonexistentDirectories(pathsToCreate, userId)
                    .stream()
                    .map(resource -> ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resource, null))
                    .toList());

            repository.uploadFile(fileData.fullPath(), file);
            responses.add(ResourceInfoResponseAssembler.resourceToResourceInfoResponse(fileData, file.getSize()));
        }
        return responses;
    }

    @Override
    public ResourceDownloadData downloadResource(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.path());
        checkResourceExists(resourceData.fullPath(), resourceData.resourceType());

        ResourceWriter resourceWriter = resourceData.resourceType() == DIRECTORY ? downloadDirectory(resourceData) : downloadFile(resourceData);

        return new ResourceDownloadData(resourceData.resourceName(), resourceWriter, resourceData.resourceType());
    }

    @Override
    public ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo) {
        Resource pathFromData = createFromUserInput(userId, pathFrom);
        checkPathExists(pathFromData.path());
        checkResourceExists(pathFromData.fullPath(), pathFromData.resourceType());

        Resource pathToData = createFromUserInput(userId, pathTo);

        boolean isRename = !pathToData.resourceName().equals(pathFromData.resourceName());
        boolean isMoving = !pathToData.path().equals(pathFromData.path());

        if (isMoving == isRename) {
            throw new InvalidResourceOperationException(INVALID_OPERATION_COMBINATION);
        }

        checkPathExists(pathToData.path());

        if (pathToData.resourceType() != pathFromData.resourceType()) {
            throw new InvalidResourceTypeChangeException(INVALID_RESOURCE_TYPE_CHANGE);
        }

        ResourceInfoResponse response;
        if (pathFromData.resourceType() == FILE) {
            moveResource(pathFromData.fullPath(), pathToData.fullPath());
            StatObjectResponse resourceInfo = repository.getResourceInfo(pathToData.fullPath());
            response = ResourceInfoResponseAssembler.resourceToResourceInfoResponse(pathToData, resourceInfo.size());
        } else {
            Set<String> resourcesItemsByPrefix = repository.getResourcesItemsByPrefix(pathFromData.fullPath(), true).keySet();
            for (String oldPath : resourcesItemsByPrefix) {
                String newPath = oldPath.replace(pathFromData.fullPath(), pathToData.fullPath());
                moveResource(oldPath, newPath);
            }
            response = ResourceInfoResponseAssembler.resourceToResourceInfoResponse(pathToData, null);
        }

        return response;
    }

    @Override
    public List<ResourceInfoResponse> searchResource(Long userId, String query) {
        Resource pathData = createFromUserInput(userId, query);
        List<ResourceInfoResponse> resourcesByPrefix = getResourcesByPrefix(pathData.userFolder(), userId, true);

        return resourcesByPrefix.stream()
                .filter(r -> r.name().toLowerCase()
                        .contains(query.toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkResourceExists(resourceData.path(), resourceData.resourceType());
        checkPathEndWithSlash(resourceData.fullPath());

        if (repository.checkResourceExists(resourceData.fullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.fullPath());
        return ResourceInfoResponseAssembler.resourceToResourceInfoResponse(resourceData, null);
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        Resource resourceData = createFromUserInput(userId, path);

        checkPathExists(resourceData.path());
        checkResourceExists(resourceData.fullPath(), DIRECTORY);

        return getResourcesByPrefix(resourceData.fullPath(), userId, false);
    }

    @Override
    public Long createUserDirectoryPath(Long userId) {
        String userFolder = ResourceFactory.getUserDirectoryPath(userId);
        repository.uploadDirectory(userFolder);
        return userId;
    }

    private void deleteDirectory(Resource resourceData) {
        Map<String, Item> resourcesItems = repository.getResourcesItemsByPrefix(resourceData.fullPath(), true);

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

    private ResourceWriter downloadDirectory(Resource resourceData) {
        Set<String> resourceToDownloadNames = repository.getResourcesItemsByPrefix(resourceData.fullPath(), true).keySet();
        resourceToDownloadNames.remove(resourceData.fullPath());
        return outputStream -> {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

                for (String resourceToDownloadName : resourceToDownloadNames) {
                    try (InputStream resourceToDownload = repository.downloadResource(resourceToDownloadName)) {
                        String prefixToRemove = resourceData.fullPath();
                        String pathInsideZip = resourceToDownloadName.startsWith(prefixToRemove)
                                ? resourceToDownloadName.substring(prefixToRemove.length())
                                : resourceToDownloadName;

                        ZipEntry entry = new ZipEntry(pathInsideZip);
                        zipOutputStream.putNextEntry(entry);
                        resourceToDownload.transferTo(zipOutputStream);
                        zipOutputStream.closeEntry();
                    }
                }
            }
        };
    }

    private ResourceWriter downloadFile(Resource resourceData) {
        return outputStream -> {
            try (InputStream resourceToDownload = repository.downloadResource(resourceData.fullPath())) {
                resourceToDownload.transferTo(outputStream);
            }
        };
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