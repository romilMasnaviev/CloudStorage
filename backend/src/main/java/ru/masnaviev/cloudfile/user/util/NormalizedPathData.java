package ru.masnaviev.cloudfile.user.util;

import lombok.Getter;

@Getter
public class NormalizedPathData {
    String userId;
    String path;
    String filename;
    String fullPath;

    public NormalizedPathData(Long userId, String path, String filename) {
        this.userId = getNormalizedUserFolderFromUserId(userId);

        this.fullPath = path.equals("/") ?
                this.userId + path + filename :
                this.userId + "/" + path + filename;

        this.filename = fullPath.substring(fullPath.lastIndexOf("/") + 1);

        this.path = (fullPath.indexOf("/") == fullPath.lastIndexOf("/") ?
                "" :
                fullPath.substring(fullPath.indexOf("/") + 1, fullPath.lastIndexOf("/") + 1));
    }

    public static String getNormalizedUserFolderFromUserId(Long userId) {
        return "user-" + userId + "-files";
    }
}
