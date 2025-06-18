package ru.masnaviev.cloudfile.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.custom.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.UserRepository;
import ru.masnaviev.cloudfile.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    private static final String username = "username";
    private static final String password = "password";

    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserService service;
    @Autowired
    private UserRepository repository;

    @Autowired
    private JdbcTemplate template;

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);

        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
    }

    @BeforeEach
    void dbClear() {
        template.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void whenUserExists_thenTrowUserAlreadyExistsException() {
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);
        addTestUser();

        long initialUsersCount = repository.count();
        var exception = assertThrows(UserAlreadyExistsException.class, () -> service.registration(request));
        long actualUsersCount = repository.count();

        assertEquals("User with this username already exists", exception.getMessage());
        assertEquals(0, actualUsersCount - initialUsersCount);
    }

    @Test
    void whenUserDoesntExist_thenReturnUserRegistrationResponse() {
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);
        long initialUsersCount = repository.count();

        UserRegistrationResponse response = service.registration(request);
        long actualUsersCount = repository.count();

        assertEquals(username, response.username());
        assertEquals(1, actualUsersCount - initialUsersCount);
        assertTrue(repository.findByUsername(username).isPresent());
    }


    private void addTestUser() {
        User user = new User();
        user.setPassword(password);
        user.setUsername(username);
        repository.save(user);
    }

}