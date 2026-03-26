package ru.masnaviev.cloudstorage.service;

import ru.masnaviev.cloudstorage.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudstorage.dto.response.user.UserRegistrationResponse;

public interface UserService {
    UserRegistrationResponse registration(UserRegistrationRequest request);

    Long getIdByUsername(String username);
}
