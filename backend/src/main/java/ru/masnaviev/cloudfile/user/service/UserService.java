package ru.masnaviev.cloudfile.user.service;

import ru.masnaviev.cloudfile.user.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.user.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registration(UserRegistrationRequest request);
}
