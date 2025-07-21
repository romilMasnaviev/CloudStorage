package ru.masnaviev.cloudfile.user.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudfile.user.dto.response.resource.ResourceInfoResponse;
import ru.masnaviev.cloudfile.user.exception.resource.DirectoryNotFoundException;
import ru.masnaviev.cloudfile.user.exception.resource.FileNotFoundException;
import ru.masnaviev.cloudfile.user.exception.resource.PathNotFoundException;
import ru.masnaviev.cloudfile.user.exception.resource.ResourceNotFoundException;
import ru.masnaviev.cloudfile.user.util.NormalizedResourceData;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.DIRECTORY;
import static ru.masnaviev.cloudfile.user.dto.response.resource.ResourceType.FILE;

@Component
@RequiredArgsConstructor
public class S3FileService {

    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public ResourceInfoResponse getResourceInfo(Long userId, String path) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData);

        return resourceData.getResourceType() == DIRECTORY ?
                getFolderInfo(resourceData) :
                getFileInfo(resourceData);
    }

    private ResourceInfoResponse getFolderInfo(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());

        results.forEach(r -> {
        });

        if (results.iterator().hasNext()) {
            return ResourceInfoResponse.builder()
                    .path(resourceData.getPathWithoutUsernameAndFilename())
                    .name(resourceData.getResourceName())
                    .size(null)
                    .resourceType(DIRECTORY)
                    .build();
        } else {
            throw new ResourceNotFoundException(RESOURCE_NOT_FOUND);
        }
    }

    private ResourceInfoResponse getFileInfo(NormalizedResourceData resourceData) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        StatObjectResponse response = client.statObject(StatObjectArgs.builder()
                .bucket(minioBucketName)
                .object(resourceData.getFullPath())
                .build());

        return ResourceInfoResponse.builder()
                .path(resourceData.getPathWithoutUsernameAndFilename())
                .name(resourceData.getResourceName())
                .size(response.size())
                .resourceType(FILE)
                .build();
    }

    public void deleteResource(Long userId, String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        NormalizedResourceData resourceData = new NormalizedResourceData(userId, path);

        checkPathExists(resourceData);

        if (resourceData.getResourceType() == DIRECTORY) {
            checkDirectoryExists(resourceData);
            deleteFolder(resourceData);
        } else {
            checkFileExists(resourceData);
            deleteFile(resourceData);
        }
    }

    private void checkDirectoryExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        if (!results.iterator().hasNext()) {
            throw new DirectoryNotFoundException(DIRECTORY_NOT_FOUND);
        }
    }

    private void checkFileExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        if (!results.iterator().hasNext()) {
            throw new FileNotFoundException(FILE_NOT_FOUND);
        }
    }

    private void checkPathExists(NormalizedResourceData resourceData) {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getPathWithoutFilename())
                .maxKeys(1)
                .build());
        results.forEach(r -> {
        });
        if (!results.iterator().hasNext()) {
            throw new PathNotFoundException(PATH_NOT_FOUND);
        }
    }


    private void deleteFile(NormalizedResourceData resourceData) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(minioBucketName)
                .object(resourceData.getFullPath())
                .build());
    }

    private void deleteFolder(NormalizedResourceData resourceData) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<DeleteObject> deleteObjects = new ArrayList<>();

        Iterable<Result<Item>> resourcesForDelete = client.listObjects(ListObjectsArgs.builder()
                .bucket(minioBucketName)
                .prefix(resourceData.getFullPath())
                .recursive(true)
                .build());


        for (Result<Item> result : resourcesForDelete) {
            deleteObjects.add(new DeleteObject(result.get().objectName()));
        }

        Iterable<Result<DeleteError>> deletedResources = client.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioBucketName)
                .objects(deleteObjects)
                .build());

        deletedResources.forEach(r -> {
        });
    }


//    public List<UploadedResource> uploadResources(Long userId, String path, List<MultipartFile> files) {
//
//        List<UploadedResource> uploadedResources = new ArrayList<>();
//
//        for (MultipartFile file : files) {
////            var pathData = new NormalizedPathData(userId, path, file.getOriginalFilename());
////            checkFileAlreadyExists(pathData.getFullPath());
////            UploadedResource uploadedResource = uploadFile(pathData, file);
////            uploadedResources.add(uploadedResource);
//        }
//        возврат созданных папок
//
//        return uploadedResources;
//    }
//
//    private UploadedResource uploadFile(NormalizedResourceData path, MultipartFile file) {
//        try (var inputStream = new BufferedInputStream(file.getInputStream())) {
//            client.putObject((PutObjectArgs.builder()
//                    .bucket(minioBucketName)
//                    .object(path.getFullPath())
//                    .stream(inputStream, -1, 100 * 1024 * 1024)
//                    .build()));
//
////            return new UploadedResource(path.getPath(), path.getFilename(), file.getSize(), Type.FILE);
//        } catch (IOException ex) {
//            throw new FileReadException(FILE_READ_ERROR, ex);
//        } catch (Exception ex) {
//            throw new RuntimeException(UNEXPECTED_FILE_UPLOAD_EXCEPTION, ex);
//        }
//        return null;
//    }
//
//    private void checkFileAlreadyExists(String fullFilePath) {
//        try {
//            client.statObject(StatObjectArgs.builder()
//                    .bucket(minioBucketName)
//                    .object(fullFilePath)
//                    .build());
//            throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
//        } catch (ErrorResponseException ex) {
//            if (!ex.errorResponse().code().equals("NoSuchKey")) {
//                throw new RuntimeException(ex);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}

