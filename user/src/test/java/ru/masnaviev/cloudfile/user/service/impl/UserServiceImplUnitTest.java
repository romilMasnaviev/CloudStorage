package ru.masnaviev.cloudfile.user.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.custom.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    private static final String username = "username";
    private static final String password = "password";

    @Mock
    private UserRepository repository;
    @Spy
    private PasswordEncoder encoder = new BCryptPasswordEncoder();
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void whenUserExists_thenTrowUserAlreadyExistsException() {
        when(repository.existsByUsername(eq(username))).thenReturn(true);
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);

        var exception = assertThrows(UserAlreadyExistsException.class, () -> service.registration(request));

        Assertions.assertEquals("User with this username already exists", exception.getMessage());
        verify(encoder, never()).encode(password);
        verify(repository, never()).save(any());
    }

    @Test
    void whenUserDoesntExist_thenReturnUserRegistrationResponse() {
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);
        when(repository.existsByUsername(eq(username))).thenReturn(false);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        when(repository.save(any(User.class))).thenReturn(user);

        UserRegistrationResponse response = service.registration(request);

        Assertions.assertEquals(request.username(), response.username());
        verify(encoder).encode(password);
        verify(repository).save(any());
    }


}