package ru.masnaviev.cloudfile.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudfile.user.exception.ErrorResponse;
import ru.masnaviev.cloudfile.user.service.AuthService;
import ru.masnaviev.cloudfile.user.service.UserService;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.*;

@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(
            summary = "User Registration",
            description = "Register a new user in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User already logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Tag(name = "authentication")
    @PostMapping(AUTH_SIGN_UP_URL)
    public ResponseEntity<UserRegistrationResponse> registration(@RequestBody @Valid UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.registration(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "User Authorization", description = "Authorize a user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserAuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User already logged in",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Tag(name = "authentication")
    @PostMapping(AUTH_SIGN_IN_URL)
    public ResponseEntity<UserAuthorizationResponse> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                                                   HttpServletRequest servletRequest) {
        UserAuthorizationResponse response = authService.authorization(request, servletRequest);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "Logout",
            description = "End user session"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully logged out", content = @Content()),
            @ApiResponse(responseCode = "401", description = "User not authorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Tag(name = "authentication")
    @PostMapping(AUTH_SIGN_OUT_URL)
    public ResponseEntity<?> logout(HttpServletRequest servletRequest) {
        authService.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }

}