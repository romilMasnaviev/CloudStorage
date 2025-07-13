package ru.masnaviev.cloudfile.user.dto.response.storage;

import lombok.Data;

@Data
public class UploadedFile {
    String path;
    String name;
    long size;
    Type type;

    public UploadedFile(String path, String name, long size, Type type) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.type = type;
    }
}
