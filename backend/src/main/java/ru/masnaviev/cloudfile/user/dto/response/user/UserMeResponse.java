package ru.masnaviev.cloudfile.user.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information about the current user")
public record UserMeResponse(
        @Schema(description = "Username", example = "username") String username) {
}