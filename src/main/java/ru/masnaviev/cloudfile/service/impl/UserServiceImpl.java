package ru.masnaviev.cloudfile.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.masnaviev.cloudfile.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudfile.exception.user.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.model.User;
import ru.masnaviev.cloudfile.repository.UserRepository;
import ru.masnaviev.cloudfile.service.UserService;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.USERNAME_NOT_FOUND;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.USER_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserRegistrationResponse registration(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS);
        }
        User savedUser = userRepository.save(createUser(request));
        return new UserRegistrationResponse(savedUser.getUsername());
    }

    public Long getIdByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(USERNAME_NOT_FOUND));
        return user.getId();
    }

    private User createUser(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;
    }
}
