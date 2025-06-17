package ru.masnaviev.cloudfile.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.masnaviev.cloudfile.user.dto.user.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.user.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.Role;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.RoleRepository;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationResponse registration(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("User with this username already exists");
        }
        User user = createUser(request);
        userRepository.save(user);
        return new UserRegistrationResponse(user.getUsername());
    }

    private User createUser(UserRegistrationRequest request) {
        User user = new User();
        Role role = roleRepository.findByRole("ROLE_USER")
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        user.setRoles(List.of(role));
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        return user;
    }
}
