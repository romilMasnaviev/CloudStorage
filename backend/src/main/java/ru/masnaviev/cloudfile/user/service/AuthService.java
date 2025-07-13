package ru.masnaviev.cloudfile.user.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.masnaviev.cloudfile.user.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.response.user.UserAuthorizationResponse;

public interface AuthService {
    UserAuthorizationResponse authorization(UserAuthorizationRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest servletRequest);
}
