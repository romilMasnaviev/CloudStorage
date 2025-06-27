package ru.masnaviev.cloudfile.user.service;

import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registration(UserRegistrationRequest request);
}
