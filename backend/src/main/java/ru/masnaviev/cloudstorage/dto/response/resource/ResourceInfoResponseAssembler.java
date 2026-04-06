package ru.masnaviev.cloudstorage.dto.response.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudstorage.model.Resource;

import static ru.masnaviev.cloudstorage.util.ResourceType.DIRECTORY;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceInfoResponseAssembler {

    public static ResourceInfoResponse resourceToResourceInfoResponse(Resource resource, Long size) {
        String resourceName = resource.getResourceType() == DIRECTORY ? resource.getResourceName() + "/" : resource.getResourceName();
        String path = resource.getPathWithoutUserFolder().startsWith("/") ? resource.getPathWithoutUserFolder().replaceFirst("/", "") : resource.getPathWithoutUserFolder();
        return new ResourceInfoResponse(path, resourceName, size, resource.getResourceType());
    }
}
