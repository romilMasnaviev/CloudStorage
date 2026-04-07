package ru.masnaviev.cloudstorage.assembler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudstorage.model.Resource;

import static ru.masnaviev.cloudstorage.model.ResourceType.DIRECTORY;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceInfoResponseAssembler {

    public static ResourceInfoResponse resourceToResourceInfoResponse(Resource resource, Long size) {
        String resourceName = resource.resourceType() == DIRECTORY ? resource.resourceName() + "/" : resource.resourceName();
        String path = resource.pathWithoutUserFolder().startsWith("/") ?
                resource.pathWithoutUserFolder().replaceFirst("/", "") :
                resource.pathWithoutUserFolder();
        return new ResourceInfoResponse(path, resourceName, size, resource.resourceType());
    }
}
