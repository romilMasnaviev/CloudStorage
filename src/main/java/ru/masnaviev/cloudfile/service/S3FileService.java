package ru.masnaviev.cloudfile.service;

import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudfile.dto.response.resource.ResourceInfoResponse;

import java.util.List;

public interface S3FileService {

    ResourceInfoResponse getResourceInfo(Long userId, String path);

    void deleteResource(Long userId, String path);

    DownloadResourceResponse downloadResource(Long userId, String path);

    ResourceInfoResponse uploadDirectory(Long userId, String path);

    List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path);

    List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files);

    ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo);

    void createUserDirectory(Long userId);
}