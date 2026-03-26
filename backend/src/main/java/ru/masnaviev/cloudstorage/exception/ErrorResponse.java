package ru.masnaviev.cloudstorage.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Структура ответа при ошибке API")
public record ErrorResponse(
        @Schema(description = "Сообщение об ошибке", example = "Неверный запрос")
        String message) {
}