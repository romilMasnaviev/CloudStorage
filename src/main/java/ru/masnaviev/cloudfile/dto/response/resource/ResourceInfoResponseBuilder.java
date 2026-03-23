package ru.masnaviev.cloudfile.dto.response.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudfile.util.ResourceType;


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
}
