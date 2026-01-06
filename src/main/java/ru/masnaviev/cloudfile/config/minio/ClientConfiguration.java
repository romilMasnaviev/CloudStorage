package ru.masnaviev.cloudfile.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@RequiredArgsConstructor
class ClientConfiguration {

    private final Environment env;
    @Value("${minio.bucket.name}")
    private String minioBucketName;

    @Bean
    MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(env.getProperty("minio.endpoint"))
                .credentials(env.getProperty("minio.username"), env.getProperty("minio.password"))
                .build();

        createBucketForFiles(minioBucketName, client);

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
            throw new RuntimeException(e);
        }
    }
}
