package ru.masnaviev.cloudfile.user.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.masnaviev.cloudfile.user.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.user.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.masnaviev.cloudfile.user.TestData.*;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.USER_ALREADY_EXISTS;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserRepository repository;
    @Spy
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void registerUser_whenUserExists_thenThrowUserAlreadyExistsException() {
        when(repository.existsByUsername(eq(USERNAME))).thenReturn(true);
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(USERNAME, PASSWORD);

        var exception = assertThrows(UserAlreadyExistsException.class, () -> service.registration(registrationRequest));

        assertEquals(USER_ALREADY_EXISTS, exception.getMessage());
        verify(encoder, never()).encode(PASSWORD);
        verify(repository, never()).save(any());
    }

    @Test
    void registerUser_whenUserDoesNotExist_thenReturnUserRegistrationResponse() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(USERNAME, PASSWORD);
        when(repository.existsByUsername(eq(USERNAME))).thenReturn(false);
        User savedUser = createUser(USERNAME, PASSWORD);
        when(repository.save(any(User.class))).thenReturn(savedUser);

        UserRegistrationResponse registrationResponse = service.registration(registrationRequest);

        assertEquals(registrationRequest.username(), registrationResponse.username());
        verify(encoder).encode(PASSWORD);
        verify(repository).save(any());
    }
}