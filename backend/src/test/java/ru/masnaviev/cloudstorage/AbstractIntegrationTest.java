package ru.masnaviev.cloudstorage;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractIntegrationTest {

    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    public static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:8-alpine"));

    public static MinIOContainer minIOContainer = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

    static {
        postgreSQLContainer.start();
        redisContainer.start();
        minIOContainer.start();
    }

    @DynamicPropertySource
    static void property(DynamicPropertyRegistry registry) {
        postgresConfiguration(registry);
        flywayConfiguration(registry);
        redisConfiguration(registry);
        minioConfiguration(registry);
    }

    private static void postgresConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    private static void flywayConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
    }

    private static void redisConfiguration(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    private static void minioConfiguration(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", () -> minIOContainer.getS3URL());
        registry.add("minio.username", () -> minIOContainer.getUserName());
        registry.add("minio.password", () -> minIOContainer.getPassword());
        registry.add("minio.bucketname", () -> "user-files");
    }
}