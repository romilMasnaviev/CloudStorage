package ru.masnaviev.cloudfile.user.dto.response.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UploadFilesResponse {
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    public void addFile(UploadedFile file) {
        uploadedFiles.add(file);
    }
}
