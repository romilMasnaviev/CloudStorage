package ru.masnaviev.cloudfile.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.USERID_MUST_NOT_BE_LESS_0;
import static ru.masnaviev.cloudfile.util.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.util.ResourceType.FILE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceBuilder {

    public static Resource createFrom(Long userId, String path) {

        validPath(path);

        validUserId(userId);

        String userFolder = initUserFolder(userId);

        if (path.startsWith(userFolder)) {
            path = path.substring(userFolder.length());
        }

        ResourceType resourceType = path.endsWith("/") ? DIRECTORY : FILE;

        String fullPath = initFullPath(path, userFolder, resourceType);

        String resourceName = initResourceName(path, fullPath);

        String pathWithoutResourceName = initPathWithoutResourceName(fullPath, resourceName);

        String substring = path.substring(0, path.lastIndexOf(resourceName));

        String pathWithoutUsernameAndResourceName = substring.isEmpty() ? "/" : substring;

        List<String> pathsList = getPaths(pathWithoutResourceName, userFolder);

        return new Resource(userId, fullPath, resourceType, resourceName, userFolder, pathWithoutResourceName, pathWithoutUsernameAndResourceName, pathsList);
    }

    private static void validPath(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException(PATH_MUST_NOT_BE_EMPTY);
        }
    }

    private static void validUserId(Long userId) {
        if (userId < 0L) {
            throw new IllegalArgumentException(USERID_MUST_NOT_BE_LESS_0);
        }
    }

    private static String initPathWithoutResourceName(String fullPath, String resourceName) {
        return fullPath.substring(0, fullPath.lastIndexOf(resourceName));
    }

    private static String initResourceName(String path, String fullPath) {
        return path.equals("/") ? "" : List.of(fullPath.split("/")).getLast();
    }

    private static String initFullPath(String path, String userFolder, ResourceType resourceType) {
        Path result = Path.of(userFolder, path).normalize();

        if (resourceType == DIRECTORY) {
            String stringPath = result.toString();
            return stringPath.endsWith("/") ? stringPath : stringPath + "/";
        }

        return result.toString();
    }

    private static String initUserFolder(Long userId) {
        return "user-" + userId + "-files";
    }

    private static List<String> getPaths(String pathWithoutResourceName, String userFolder) {
        StringBuilder sb = new StringBuilder();
        List<String> folders = new ArrayList<>(Arrays.stream(pathWithoutResourceName.split("/")).toList());
        List<String> pathsList = new ArrayList<>();
        for (String folder : folders) {
            sb.append(folder).append("/");
            pathsList.add(String.valueOf(sb));
        }
        if (pathsList.getFirst().equals(userFolder + "/")) {
            pathsList.removeFirst();
        }
        return pathsList;
    }
}
