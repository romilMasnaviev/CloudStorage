package ru.masnaviev.cloudfile.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.masnaviev.cloudfile.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.dto.response.user.UserAuthorizationResponse;

public interface AuthService {
    UserAuthorizationResponse authorization(UserAuthorizationRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest servletRequest);
}
