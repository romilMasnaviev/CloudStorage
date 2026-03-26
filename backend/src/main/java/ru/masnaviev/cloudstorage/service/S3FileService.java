package ru.masnaviev.cloudstorage.service;

import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudstorage.dto.response.resource.DownloadResourceResponse;
import ru.masnaviev.cloudstorage.dto.response.resource.ResourceInfoResponse;

import java.util.List;

public interface S3FileService {

    ResourceInfoResponse getResourceInfo(Long userId, String path);

    void deleteResource(Long userId, String path);

    List<ResourceInfoResponse> uploadResources(Long userId, String path, List<MultipartFile> files);

    DownloadResourceResponse downloadResource(Long userId, String path);

    ResourceInfoResponse moveResource(Long userId, String pathFrom, String pathTo);

    List<ResourceInfoResponse> searchResource(Long userId, String query);

    ResourceInfoResponse uploadDirectory(Long userId, String path);

    List<ResourceInfoResponse> getDirectoryContentsInfo(Long userId, String path);

    void createUserDirectory(Long userId);
}