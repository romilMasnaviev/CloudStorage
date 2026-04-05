package ru.masnaviev.cloudstorage.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.masnaviev.cloudstorage.AbstractIntegrationTest;
import ru.masnaviev.cloudstorage.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudstorage.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudstorage.exception.user.UserAlreadyExistsException;
import ru.masnaviev.cloudstorage.repository.UserRepository;
import ru.masnaviev.cloudstorage.service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static ru.masnaviev.cloudstorage.TestData.*;
import static ru.masnaviev.cloudstorage.constants.ErrorMessages.USER_ALREADY_EXISTS;

@SpringBootTest
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