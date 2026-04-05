package ru.masnaviev.cloudstorage.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.masnaviev.cloudstorage.exception.resource.MinioOperationException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@RequiredArgsConstructor
class ClientConfiguration {

    private final MinioProperties minioProperties;

    @Bean
    MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.username(), minioProperties.password())
                .build();
        createBucketForFiles(minioProperties.bucketName(), client);

        return client;
    }

    private void createBucketForFiles(String bucketName, MinioClient client) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName).build())) {
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName).build());
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new MinioOperationException(e);
        }
    }
}
