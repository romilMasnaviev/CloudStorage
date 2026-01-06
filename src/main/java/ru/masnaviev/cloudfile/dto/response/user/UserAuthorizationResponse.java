package ru.masnaviev.cloudfile.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for successful user authorization")
public record UserAuthorizationResponse(
        @Schema(description = "Username", example = "username") String username) {
}