package ru.masnaviev.cloudstorage.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.masnaviev.cloudstorage.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudstorage.dto.response.user.UserAuthorizationResponse;

public interface AuthService {
    UserAuthorizationResponse authorization(UserAuthorizationRequest request, HttpServletRequest servletRequest);

    void logout(HttpServletRequest servletRequest);
}
