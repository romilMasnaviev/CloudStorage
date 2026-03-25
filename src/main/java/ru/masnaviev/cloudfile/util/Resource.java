package ru.masnaviev.cloudfile.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Resource {
    private final Long userId;
    private final String fullPath;
    private final ResourceType resourceType;
    private final String resourceName;
    private final String userFolder;
    private final String path;
    private final String pathWithoutUserFolder;
    private final List<String> pathsList;

    protected Resource(Long userId, String fullPath, ResourceType resourceType, String resourceName, String userFolder, String path, String pathWithoutUserFolder, List<String> pathsList) {
        this.userId = userId;
        this.fullPath = fullPath;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.userFolder = userFolder;
        this.path = path;
        this.pathWithoutUserFolder = pathWithoutUserFolder;
        this.pathsList = pathsList;
    }

    public List<String> getPathsList() {
        return new ArrayList<>(pathsList);
    }
}

