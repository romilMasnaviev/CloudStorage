package ru.masnaviev.cloudfile.dto.response.resource;

import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudfile.util.ResourceType;

import java.nio.file.Path;
import java.nio.file.Paths;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceInfoResponseBuilder {

    public static ResourceInfoResponse createFrom(String path, String name, Long size, ResourceType resourceType) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        if (name.startsWith("/")) {
            name = name.replaceFirst("/", "");
        }

        return new ResourceInfoResponse(path, name, size, resourceType);
    }

    public static ResourceInfoResponse createFrom(Item item) {
        Path path = Paths.get(item.objectName());
        ResourceType resourceType = item.objectName().endsWith("/") ? ResourceType.DIRECTORY : ResourceType.FILE;
        Long size = resourceType == ResourceType.DIRECTORY ? null : item.size();
        String name = path.getFileName().toString();

        String responsePath;
        if (path.getNameCount() <= 2) {
            responsePath = "";
        } else {
            responsePath = path.subpath(1,path.getNameCount()-1) + "/";
        }
        return new ResourceInfoResponse(responsePath, name, size, resourceType);
    }


}
