package ru.masnaviev.cloudfile.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

public record UserAuthorizationRequest(
        @NotBlank(message = USERNAME_MUST_NOT_BE_EMPTY)
        @Size(min = 8, max = 50, message = USERNAME_LENGTH_BETWEEN_8_50)
        String username,

        @NotBlank(message = PASSWORD_MUST_NOT_BE_EMPTY)
        @Size(min = 8, max = 100, message = PASSWORD_LENGTH_BETWEEN_8_100)
        String password) {
}
