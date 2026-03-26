package ru.masnaviev.cloudstorage.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о текущем пользователе")
public record UserMeResponse(
        @Schema(description = "Имя пользователя", example = "username") String username) {
}