package ru.masnaviev.cloudfile.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.masnaviev.cloudfile.user.dto.user.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.user.response.UserCreateResponse;
import ru.masnaviev.cloudfile.user.exception.UserAlreadyExistsException;
import ru.masnaviev.cloudfile.user.model.Role;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.RoleRepository;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public ResponseEntity<?> registration(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("User already exists");
        } else {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(encoder.encode(request.getPassword()));
            Role role = roleRepository.findByRole("USER");
            user.setRoles(List.of(role));

            userRepository.save(user);
            String username = user.getUsername();
            UserCreateResponse response = new UserCreateResponse();
            response.setUsername(username);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(201));
        }
    }
}
