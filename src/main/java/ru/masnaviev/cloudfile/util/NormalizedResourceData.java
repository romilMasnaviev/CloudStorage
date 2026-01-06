package ru.masnaviev.cloudfile.util;

import lombok.Data;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudfile.dto.response.resource.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.dto.response.resource.ResourceType.FILE;

@Data
public class NormalizedResourceData {
    // example 1
    Long userId;
    // example "user-1-files"
    String userFolder;
    // example : "folder1","folder2","folder3"
    List<String> folders;
    ResourceType resourceType;
    //Folder example : folder2
    //File example : file.txt
    String resourceName;

    // Folder example : user-1-files/folder1/folder2/
    // File example : user-1-files/folder1/folder2/folder3/file.txt
    String fullPath;

    public NormalizedResourceData(Long userId, String path) {
        this.userId = userId;
        parseUserFolder(userId);
        parseFoldersAndResourceName(path);
        createFullPath();
        System.out.println(this);
    }

    public static void main(String[] args) {
        NormalizedResourceData normalizedResourceData = new NormalizedResourceData(3L, "user-3-files/1/2/3/4/5/6/Задание.jpg");
        System.out.println("normalizedResourceData = " + normalizedResourceData);
        System.out.println("normalizedResourceData.getPathsList() = " + normalizedResourceData.getPathsList());
    }

    @Override
    public String toString() {
        return "NormalizedResourceData{" +
                "userId=" + userId +
                ", userFolder='" + userFolder + '\'' +
                ", folders=" + folders +
                ", type=" + resourceType +
                ", resourceName='" + resourceName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }

    private void parseUserFolder(Long userId) {
        userFolder = "user-" + userId + "-files";
    }

    private void createFullPath() {
        String path;

        if (folders.isEmpty()) {
            if (resourceName.isEmpty()) {
                path = userFolder;
            } else {
                path = String.join("/", userFolder, resourceName);
            }
        } else {
            path = String.join("/", userFolder, String.join("/", folders), resourceName);
        }

        if (resourceType == DIRECTORY) {
            fullPath = path.endsWith("/") ? path : path + "/";
        } else {
            fullPath = path;
        }
    }

    public String getPathWithoutUsernameAndResourceName() {
        return folders.isEmpty() ? "/" : String.join("/", folders) + "/";
    }

    public String getPathWithoutResourceName() {
        if (folders.isEmpty()) {
            return userFolder + "/";
        }
        return userFolder + "/" + String.join("/", folders) + "/";
    }

    public List<String> getPathsList() {
        List<String> paths = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder().append(userFolder).append("/");
        for (String folder : folders) {
            stringBuilder.append(folder).append("/");
            paths.add(String.valueOf(stringBuilder));
        }
        return paths;
    }

    private void parseFoldersAndResourceName(String path) {
        if (path.endsWith("/")) {
            resourceType = DIRECTORY;
            if (path.indexOf("/") != path.length() - 1) {
                String pathWithoutSlashAtEnd = path.substring(0, path.length() - 1);
                resourceName = pathWithoutSlashAtEnd.substring(pathWithoutSlashAtEnd.lastIndexOf("/") + 1);
                String pathWithoutResourceName = pathWithoutSlashAtEnd.substring(0, pathWithoutSlashAtEnd.lastIndexOf("/"));
                folders = Arrays.stream(pathWithoutResourceName.split("/")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            } else {
                resourceName = path.substring(0, path.length() - 1);
                folders = List.of();
            }
        } else {
            resourceType = FILE;
            if (path.contains("/")) {
                resourceName = path.substring(path.lastIndexOf("/") + 1);
                String pathWithoutResourceName = path.substring(0, path.lastIndexOf("/"));
                folders = Arrays.stream(pathWithoutResourceName.split("/")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            } else {
                resourceName = path;
                folders = List.of();
            }
        }
    }
}
