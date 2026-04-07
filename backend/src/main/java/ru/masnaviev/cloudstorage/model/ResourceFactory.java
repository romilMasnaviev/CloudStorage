package ru.masnaviev.cloudstorage.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.PATH_MUST_NOT_BE_EMPTY;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.USERID_MUST_NOT_BE_LESS_0;
import static ru.masnaviev.cloudstorage.model.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudstorage.model.ResourceType.FILE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFactory {

    public static Resource createFromUserInput(Long userId, String path) {
        validatePath(path);
        validateUserId(userId);

        String userFolder = buildUserFolder(userId);
        ResourceType resourceType = path.endsWith("/") ? DIRECTORY : FILE;

        String fullPath = buildFullPath(path, userFolder, resourceType);
        String resourceName = buildResourceName(path, fullPath);
        String parentFullPath = buildParentFullPath(fullPath, resourceName);
        String relativeParent = relativeParentPath(path, resourceName);

        List<String> pathsList = buildPathsList(parentFullPath, userFolder);

        return new Resource(userId, fullPath, resourceType, resourceName, userFolder,
                parentFullPath, relativeParent, pathsList);
    }


    private static String relativeParentPath(String path, String resourceName) {
        String substring = path.substring(0, path.lastIndexOf(resourceName));
        return substring.isEmpty() ? "/" : substring;
    }

    public static Resource createFromFullMinioPath(Long userId, String path) {
        String userFolder = buildUserFolder(userId);
        if (path.startsWith(userFolder)) {
            path = path.replaceFirst(userFolder, "");
        }
        return createFromUserInput(userId, path);
    }

    private static String buildUserFolder(Long userId) {
        return "user-" + userId + "-files";
    }

    public static String getUserDirectoryPath(Long userId) {
        return buildUserFolder(userId) + "/";
    }

    private static void validatePath(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException(PATH_MUST_NOT_BE_EMPTY);
        }
    }

    private static void validateUserId(Long userId) {
        if (userId < 0L) {
            throw new IllegalArgumentException(USERID_MUST_NOT_BE_LESS_0);
        }
    }

    private static String buildParentFullPath(String fullPath, String resourceName) {
        return fullPath.substring(0, fullPath.lastIndexOf(resourceName));
    }

    private static String buildResourceName(String path, String fullPath) {
        return path.equals("/") ? "" : List.of(fullPath.split("/")).getLast();
    }

    private static String buildFullPath(String path, String userFolder, ResourceType resourceType) {
        Path result = Path.of(userFolder, path).normalize();

        if (resourceType == DIRECTORY) {
            String stringPath = result.toString();
            return stringPath.endsWith("/") ? stringPath : stringPath + "/";
        }

        return result.toString();
    }

    private static List<String> buildPathsList(String pathWithoutResourceName, String userFolder) {
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
