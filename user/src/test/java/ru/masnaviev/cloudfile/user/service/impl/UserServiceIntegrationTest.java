package ru.masnaviev.cloudfile.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.masnaviev.cloudfile.user.AbstractIntegrationTest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.custom.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.repository.UserRepository;
import ru.masnaviev.cloudfile.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudfile.user.TestData.*;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.USER_ALREADY_EXISTS;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService service;
    @Autowired
    private UserRepository repository;
    @Autowired
    private JdbcTemplate template;

    @BeforeEach
    void clearDb() {
        template.execute("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void registerUser_whenUserExists_thenThrowUserAlreadyExistsException() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(USERNAME, PASSWORD);
        repository.save(createUser(USERNAME, PASSWORD));

        long initialUsersCount = repository.count();
        var exception = assertThrows(UserAlreadyExistsException.class, () -> service.registration(registrationRequest));
        long actualUsersCount = repository.count();

        assertEquals(USER_ALREADY_EXISTS, exception.getMessage());
        assertEquals(0, actualUsersCount - initialUsersCount);
    }

    @Test
    void registerUser_whenUserDoesNotExist_thenReturnUserRegistrationResponse() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(USERNAME, PASSWORD);

        long initialUsersCount = repository.count();
        UserRegistrationResponse registrationResponse = service.registration(registrationRequest);
        long actualUsersCount = repository.count();

        assertEquals(USERNAME, registrationResponse.username());
        assertEquals(1, actualUsersCount - initialUsersCount);
        assertTrue(repository.findByUsername(USERNAME).isPresent());
    }
}