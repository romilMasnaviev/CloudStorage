package ru.masnaviev.cloudstorage.dto.response.resource;

import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudstorage.util.ResourceType;

import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceInfoResponseBuilder {

    public static ResourceInfoResponse createResponseFrom(String path, String name, Long size, ResourceType resourceType) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        if (name.startsWith("/")) {
            name = name.replaceFirst("/", "");
        }

        return new ResourceInfoResponse(path, name, size, resourceType);
    }

    public static ResourceInfoResponse createResponseFrom(Item item) {
        Path path = Paths.get(item.objectName());
        ResourceType resourceType = item.objectName().endsWith("/") ? DIRECTORY : ResourceType.FILE;
        Long size = resourceType == DIRECTORY ? null : item.size();
        return getResourceInfoResponse(path, resourceType, size);
    }

    public static ResourceInfoResponse createDirectoryResponseFrom(String fullPath) {
        Path path = Paths.get(fullPath);
        Long size = null;
        return getResourceInfoResponse(path, DIRECTORY, size);
    }

    public static ResourceInfoResponse createFileResponseFrom(String fullPath, Long size) {
        Path path = Paths.get(fullPath);
        ResourceType resourceType = ResourceType.FILE;
        return getResourceInfoResponse(path, resourceType, size);
    }

    public static ResourceInfoResponse createResponseFrom(String fullPath, Long size) {
        Path path = Paths.get(fullPath);
        ResourceType resourceType = fullPath.endsWith("/") ? DIRECTORY : ResourceType.FILE;
        return getResourceInfoResponse(path, resourceType, size);
    }

    private static ResourceInfoResponse getResourceInfoResponse(Path path, ResourceType resourceType, Long size) {
        String name = path.getFileName().toString() + (resourceType == DIRECTORY ? "/" : "");

        String responsePath;
        if (path.getNameCount() <= 2) {
            responsePath = "";
        } else {
            responsePath = path.subpath(1, path.getNameCount() - 1) + "/";
        }
        return new ResourceInfoResponse(responsePath, name, size, resourceType);
    }


}
