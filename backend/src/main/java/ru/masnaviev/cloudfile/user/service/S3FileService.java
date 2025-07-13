package ru.masnaviev.cloudfile.user.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.masnaviev.cloudfile.user.dto.response.storage.Type;
import ru.masnaviev.cloudfile.user.dto.response.storage.UploadedFile;
import ru.masnaviev.cloudfile.user.exception.storage.FileAlreadyExistsException;
import ru.masnaviev.cloudfile.user.exception.storage.FileReadException;
import ru.masnaviev.cloudfile.user.util.NormalizedPathData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

@Component
@RequiredArgsConstructor
public class S3FileService {

    private final MinioClient client;

    @Value("${minio.bucket.name}")
    private String minioBucketName;

    public List<UploadedFile> uploadFiles(String path, List<MultipartFile> files, Long userId) {

        List<UploadedFile> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            var pathData = new NormalizedPathData(userId, path, file.getOriginalFilename());
            checkFileAlreadyExists(pathData.getFullPath());
            UploadedFile uploadedFile = uploadFile(pathData, file);
            uploadedFiles.add(uploadedFile);
        }

        return uploadedFiles;
    }

    private UploadedFile uploadFile(NormalizedPathData path, MultipartFile file) {
        try (var inputStream = new BufferedInputStream(file.getInputStream())) {
            client.putObject((PutObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(path.getFullPath())
                    .stream(inputStream, -1, 100 * 1024 * 1024)
                    .build()));

            return new UploadedFile(path.getPath(), path.getFilename(), file.getSize(), Type.FILE);
        } catch (IOException ex) {
            throw new FileReadException(FILE_READ_ERROR, ex);
        } catch (Exception ex) {
            throw new RuntimeException(UNEXPECTED_FILE_UPLOAD_EXCEPTION, ex);
        }
    }

    private void checkFileAlreadyExists(String fullFilePath) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(minioBucketName)
                    .object(fullFilePath)
                    .build());
            throw new FileAlreadyExistsException(FILE_ALREADY_EXIST);
        } catch (ErrorResponseException ex) {
            if (!ex.errorResponse().code().equals("NoSuchKey")) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

