package ru.masnaviev.cloudfile.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.masnaviev.cloudfile.util.NormalizedResourceData.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.NormalizedResourceData.ResourceType.FILE;

@Data
public class NormalizedResourceData {
    public final String fullPath;
    public final ResourceType resourceType;
    public final String resourceName;

    private final String userFolder;
    private final String pathWithoutResourceName;
    private final String pathWithoutUsernameAndResourceName;
    private final List<String> pathsList = new ArrayList<>();

    public NormalizedResourceData(Long userId, String path) {
        userFolder = "user-" + userId + "-files";

        fullPath = userFolder + "/" + (path.equals("/") ? "" : path);

        resourceType = path.endsWith("/") ? DIRECTORY : FILE;

        resourceName = path.equals("/") ? "" : List.of(fullPath.split("/")).getLast();

        pathWithoutResourceName = fullPath.substring(0, fullPath.lastIndexOf(resourceName));

        String substring = path.substring(0, path.lastIndexOf(resourceName));
        pathWithoutUsernameAndResourceName = substring.isEmpty() ? "/" : substring;

        StringBuilder sb = new StringBuilder();
        List<String> folders = new ArrayList<>(Arrays.stream(pathWithoutResourceName.split("/")).toList());
        for (String folder : folders) {
            sb.append(folder).append("/");
            pathsList.add(String.valueOf(sb));
        }
        if (pathsList.getFirst().equals(userFolder + "/")) {
            pathsList.removeFirst();
        }
    }

    public List<String> getPathsList() {
        return List.copyOf(pathsList);
    }

    public enum ResourceType {
        FILE, DIRECTORY
    }
}
