package ru.masnaviev.cloudfile.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error response structure")
public record ErrorResponse(
        @Schema(description = "Error message", example = "Invalid request")
        String message) {
}