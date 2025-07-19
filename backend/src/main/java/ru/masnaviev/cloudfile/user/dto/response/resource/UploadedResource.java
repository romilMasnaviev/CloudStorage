package ru.masnaviev.cloudfile.user.dto.response.resource;

import lombok.Data;

@Data
public class UploadedResource {
    String path;
    String name;
    long size;
    ResourceType resourceType;

    public UploadedResource(String path, String name, long size, ResourceType resourceType) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.resourceType = resourceType;
    }
}
