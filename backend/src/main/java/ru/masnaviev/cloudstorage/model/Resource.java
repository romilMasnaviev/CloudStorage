package ru.masnaviev.cloudstorage.model;

import java.util.ArrayList;
import java.util.List;

public record Resource(
        Long userId,
        String fullPath,
        ResourceType resourceType,
        String resourceName,
        String userFolder,
        String path,
        String pathWithoutUserFolder,
        List<String> pathsList) {
    public List<String> getPathsList() {
        return new ArrayList<>(pathsList);
    }
}

