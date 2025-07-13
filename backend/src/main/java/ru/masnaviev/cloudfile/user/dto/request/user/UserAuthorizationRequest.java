package ru.masnaviev.cloudfile.user.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

@Schema(description = "User authorization request")
public record UserAuthorizationRequest(
        @Schema(description = "User login", example = "username")
        @NotBlank(message = USERNAME_MUST_NOT_BE_EMPTY)
        @Size(min = 8, max = 50, message = USERNAME_LENGTH_BETWEEN_8_50)
        String username,

        @Schema(description = "User password", example = "password")
        @NotBlank(message = PASSWORD_MUST_NOT_BE_EMPTY)
        @Size(min = 8, max = 100, message = PASSWORD_LENGTH_BETWEEN_8_100)
        String password) {
}