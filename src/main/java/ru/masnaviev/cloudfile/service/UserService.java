package ru.masnaviev.cloudfile.service;

import ru.masnaviev.cloudfile.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.dto.response.user.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registration(UserRegistrationRequest request);

    Long getIdByUsername(String username);
}
