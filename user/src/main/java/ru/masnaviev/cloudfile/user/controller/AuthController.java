package ru.masnaviev.cloudfile.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.user.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.user.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.model.Role;
import ru.masnaviev.cloudfile.user.model.User;
import ru.masnaviev.cloudfile.user.repository.RoleRepository;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import java.util.List;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping("/api/auth/")
@AllArgsConstructor
public class AuthController {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager manager;

    @PostMapping("sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("User already exist");
        }
        User user = new User();
        Role role = roleRepository.getReferenceById(1L);
        user.setRoles(List.of(role));
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().body(user.getUsername());
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                           HttpServletRequest servletRequest) {
        var token = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Authentication authentication = manager.authenticate(token);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
        return ResponseEntity.ok(request.getUsername());
    }

    @PostMapping("sign-out")
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        servletRequest.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

}