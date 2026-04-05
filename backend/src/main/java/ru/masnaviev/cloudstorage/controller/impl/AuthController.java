package ru.masnaviev.cloudstorage.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudstorage.controller.AuthApi;
import ru.masnaviev.cloudstorage.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudstorage.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudstorage.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudstorage.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudstorage.service.AuthService;
import ru.masnaviev.cloudstorage.service.UserService;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AuthController implements AuthApi {
    private final AuthService authService;
    private final UserService userService;

    @Override
    public ResponseEntity<UserRegistrationResponse> registration(UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.registration(request);
        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<UserAuthorizationResponse> authorization(UserAuthorizationRequest request,
                                                                   HttpServletRequest servletRequest) {
        UserAuthorizationResponse response = authService.authorization(request, servletRequest);
        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<Object> logout(HttpServletRequest servletRequest) {
        authService.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }

}