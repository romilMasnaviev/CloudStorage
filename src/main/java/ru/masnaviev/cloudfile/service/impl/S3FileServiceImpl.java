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
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponseBuilder;
import ru.masnaviev.cloudfile.exception.resource.*;
import ru.masnaviev.cloudfile.repository.MinioRepository;
import ru.masnaviev.cloudfile.service.S3FileService;
import ru.masnaviev.cloudfile.util.Resource;
import ru.masnaviev.cloudfile.util.ResourceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
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

    @Override
    public ResourceInfoResponse getResourceInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());


        StatObjectResponse response = repository.getResourceInfo(resourceData.getFullPath());

        return ResourceInfoResponseBuilder.createFrom(resourceData.getPathWithoutUsernameAndResourceName(), resourceData.getResourceName(),
                resourceData.getResourceType() == FILE ? response.size() : null, resourceData.getResourceType());
    }

    @Override
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

    @Override
    public List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files) {
        Resource resourceData = createFrom(userId, path);
        checkPathExists(resourceData.getPathWithoutResourceName());

        checkPathEndWithSlash(resourceData);

        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (var file : files) {
            Resource filesData = createFrom(userId, path + file.getOriginalFilename());

            if (repository.checkResourceExists(filesData.getFullPath())) {
                throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
            }

            List<String> pathsToCreate = filesData.getPathsList();

            Set<String> existedPaths = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), true).keySet();

            pathsToCreate.removeAll(existedPaths);

            String userFolder = resourceData.getUserFolder();

            for (String pathToCreate : pathsToCreate) {
                repository.uploadDirectory(pathToCreate);

                String relativePath = pathToCreate.substring(userFolder.length());
                if (relativePath.isEmpty()) {
                    relativePath = "/";
                }

                var normalizedData = createFrom(userId, relativePath);

                ResourceInfoResponse response = ResourceInfoResponseBuilder.createFrom(
                        normalizedData.getPathWithoutUsernameAndResourceName(),
                        normalizedData.getResourceName(),
                        null,
                        normalizedData.getResourceType()
                );

                responses.add(response);
            }

            repository.uploadFile(filesData.getFullPath(), file);
            ResourceInfoResponse response = ResourceInfoResponseBuilder.createFrom(filesData.getPathWithoutUsernameAndResourceName(), filesData.getResourceName(), file.getSize(), filesData.getResourceType());

            responses.add(response);
        }
        return responses;
    }

    @Override
    public DownloadResourceResponse downloadResource(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());
        checkResourceExists(resourceData.getFullPath(), resourceData.getResourceType());

        InputStreamResource resource = resourceData.getResourceType() == DIRECTORY ?
                downloadDirectory(resourceData) :
                downloadFile(resourceData);

        return new DownloadResourceResponse(resourceData.getResourceName(), resource, resourceData.getResourceType());
    }

    @Override
    public ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo) {
        Resource pathFromData = createFrom(userId, pathFrom);
        checkPathExists(pathFromData.getPathWithoutResourceName());
        checkResourceExists(pathFromData.getFullPath(), pathFromData.getResourceType());

        Resource pathToData = createFrom(userId, pathTo);

        boolean isRename = !pathToData.getResourceName().equals(pathFromData.getResourceName());
        boolean isMoving = !pathToData.getPathWithoutResourceName().equals(pathFromData.getPathWithoutResourceName());

        if (isMoving == isRename) {
            throw new InvalidResourceOperationException(INVALID_OPERATION_COMBINATION);
        }

        checkPathExists(pathToData.getPathWithoutResourceName());


        if (pathToData.getResourceType() != pathFromData.getResourceType()) {
            throw new InvalidResourceTypeChangeException(INVALID_RESOURCE_TYPE_CHANGE);
        }

        ResourceInfoResponse response;
        if (pathFromData.getResourceType() == FILE) {
            repository.copyResource(pathFromData.getFullPath(), pathToData.getFullPath());
            repository.deleteResource(pathFromData.getFullPath());
            StatObjectResponse resourceInfo = repository.getResourceInfo(pathToData.getFullPath());
            response = ResourceInfoResponseBuilder.createFrom(pathToData.getPathWithoutUsernameAndResourceName(), pathToData.getResourceName(), resourceInfo.size(), pathToData.getResourceType());
        } else {
            Set<String> resourcesItemsByPrefix = repository.getResourcesItemsByPrefix(pathFromData.getFullPath(), true).keySet();
            Map<String, String> oldAndNewPaths = new HashMap<>();
            for (String oldPath : resourcesItemsByPrefix) {
                String newPath = oldPath.replace(pathFromData.getFullPath(), pathToData.getFullPath());
                oldAndNewPaths.put(oldPath, newPath);
            }

            for (Map.Entry<String, String> entry : oldAndNewPaths.entrySet()) {
                repository.copyResource(entry.getKey(), entry.getValue());
                repository.deleteResource(entry.getKey());
            }
            StatObjectResponse resourceInfo = repository.getResourceInfo(pathToData.getFullPath());
            response = ResourceInfoResponseBuilder.createFrom(pathToData.getPathWithoutUsernameAndResourceName(), pathToData.getResourceName(), resourceInfo.size(), pathToData.getResourceType());
        }

        return response;
    }

    @Override
    public List<ResourceInfoResponse> searchResource(Long userId, String query) {
        Resource pathData = createFrom(userId, query);
        Map<String, Item> resources = repository.getResourcesItemsByPrefix(pathData.getUserFolder(), true);
        resources.remove(pathData.getFullPath());

        List<ResourceInfoResponse> responses = new ArrayList<>();
        for (Map.Entry<String, Item> resource : resources.entrySet()) {
            var resultData = createFrom(userId, resource.getKey().replace(pathData.getUserFolder()+ "/","/"));
            if (resultData.getResourceType() != DIRECTORY && resultData.getResourceName().toLowerCase().contains(query.toLowerCase())) {
                ResourceInfoResponse response = ResourceInfoResponseBuilder.createFrom(resultData.getPathWithoutUsernameAndResourceName(), resultData.getResourceName(),
                        resource.getValue().size(), resultData.getResourceType());

                responses.add(response);
            }
        }
        return responses;
    }

    @Override
    public ResourceInfoResponse uploadDirectory(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkResourceExists(resourceData.getPathWithoutResourceName(), resourceData.getResourceType());
        checkPathEndWithSlash(resourceData);

        if (repository.checkResourceExists(resourceData.getFullPath())) {
            throw new DirectoryAlreadyExistsException(DIRECTORY_ALREADY_EXISTS);
        }

        repository.uploadDirectory(resourceData.getFullPath());

        return ResourceInfoResponseBuilder.createFrom(resourceData.getPathWithoutUsernameAndResourceName(), resourceData.getResourceName(), null, DIRECTORY);
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path) {
        Resource resourceData = createFrom(userId, path);

        checkPathExists(resourceData.getPathWithoutResourceName());

        Map<String, Item> resultMap = repository.getResourcesItemsByPrefix(resourceData.getFullPath(), false);
        //убираем родительскую папку
        resultMap.remove(resourceData.getFullPath());
        List<ResourceInfoResponse> responses = new ArrayList<>();

        for (Map.Entry<String, Item> result : resultMap.entrySet()) {

            var resultData = createFrom(userId,
                    result.getKey().replace(resourceData.getUserFolder() + "/", ""));

            ResourceInfoResponse response = ResourceInfoResponseBuilder.createFrom(resultData.getPathWithoutUsernameAndResourceName(), resultData.getResourceName(),
                    resultData.getResourceType() == DIRECTORY ? null : result.getValue().size(), resultData.getResourceType());

            responses.add(response);

        }
        return responses;
    }

    @Override
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