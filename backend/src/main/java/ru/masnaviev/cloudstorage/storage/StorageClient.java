package ru.masnaviev.cloudstorage.storage;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StorageClient {

    void uploadDirectory(String path);

    void uploadFile(String path, MultipartFile file);

    StatObjectResponse getResourceInfo(String path);

    Map<String, Item> getResourcesItemsByPrefix(String prefix, boolean recursively);

    void deleteResource(String path);

    void deleteResources(Iterable<DeleteObject> deleteObjects);

    boolean checkResourceExists(String path);

    GetObjectResponse downloadResource(String fullPath);

    void copyResource(String pathFrom, String pathTo);

}
