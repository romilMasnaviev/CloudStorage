package ru.masnaviev.cloudfile.user.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.custom.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.UserRepository;
import ru.masnaviev.cloudfile.user.service.UserService;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.USER_ALREADY_EXISTS;

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

    private User createUser(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;
    }
}
