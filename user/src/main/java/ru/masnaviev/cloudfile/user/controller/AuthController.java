package ru.masnaviev.cloudfile.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.user.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.user.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.user.response.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.dto.user.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.service.AuthService;
import ru.masnaviev.cloudfile.user.service.UserService;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.registration(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("sign-in")
    public ResponseEntity<?> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                           HttpServletRequest servletRequest) {
        UserAuthorizationResponse response = authService.authorization(request, servletRequest);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("sign-out")
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        authService.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }

}