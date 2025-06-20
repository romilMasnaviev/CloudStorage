package ru.masnaviev.cloudfile.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.service.AuthService;
import ru.masnaviev.cloudfile.user.service.UserService;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.*;

@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping(AUTH_SIGN_UP_URL)
    public ResponseEntity<?> registration(@RequestBody @Valid UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.registration(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(AUTH_SIGN_IN_URL)
    public ResponseEntity<?> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                           HttpServletRequest servletRequest) {
        UserAuthorizationResponse response = authService.authorization(request, servletRequest);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(AUTH_SIGN_OUT_URL)
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        authService.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }

}