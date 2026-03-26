package ru.masnaviev.cloudstorage.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при успешной регистрации пользователя")
public record UserRegistrationResponse(
        @Schema(description = "Имя пользователя", example = "username") String username) {
}