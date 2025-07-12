package ru.masnaviev.cloudfile.user.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.exception.storage.FileAlreadyExistsException;
import ru.masnaviev.cloudfile.user.exception.storage.FileReadException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

@Component
@RequiredArgsConstructor
public class S3FileService {
    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public void uploadFiles(String path, List<MultipartFile> files, String username) {
        checkPathEndWithSlash(path);
        checkFilesPresentInRequest(files);

        for (MultipartFile file : files) {
            String fullFilePath = createFullFilePath(username, path, file.getOriginalFilename());
            checkFileAlreadyExists(fullFilePath);
            uploadFile(fullFilePath, file);
        }
    }

    private void uploadFile(String fullFilePath, MultipartFile file) {
        try (var inputStream = new BufferedInputStream(file.getInputStream())) {
            client.putObject((PutObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(fullFilePath).stream(inputStream, -1, 100 * 1024 * 1024).build()));
        } catch (IOException ex) {
            throw new FileReadException(FILE_READ_ERROR, ex);
        } catch (Exception ex) {
            throw new RuntimeException(UNEXPECTED_FILE_UPLOAD_EXCEPTION, ex);
        }
    }

    private String createFullFilePath(String username, String path, String filename) {
        return username + "/" + path + filename;
    }

    private void checkPathEndWithSlash(String path) {
        if (!path.endsWith("/")) {
            throw new ValidationException(PATH_MUST_BE_END_SLASH);
        }
    }

    private void checkFilesPresentInRequest(List<MultipartFile> multipartFiles) {
        if (multipartFiles.isEmpty()) {
            throw new ValidationException(FILE_MUST_BE_INCLUDED_IN_REQUEST);
        }
    }

    private void checkFileAlreadyExists(String fullFilePath) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(fullFilePath)
                    .build());
            throw new FileAlreadyExistsException(FILE_ALREADY_EXISTS);
        } catch (ErrorResponseException ex) {
            if (!ex.errorResponse().code().equals("NoSuchKey")) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

