package ru.masnaviev.cloudfile.controller;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudfile.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudfile.exception.ErrorResponse;
import ru.masnaviev.cloudfile.service.AuthService;
import ru.masnaviev.cloudfile.service.S3FileService;
import ru.masnaviev.cloudfile.service.UserService;

import static ru.masnaviev.cloudfile.constatnts.ApiPath.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Tag(name = "Аутентификация", description = "API для регистрации, авторизации и управления сеансами")
class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final S3FileService s3FileServiceImpl;

    @Operation(
            summary = "Регистрация пользователя",
            description = "Регистрирует нового пользователя в системе и создает для него корневую директорию"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибки валидации",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь уже авторизован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(AUTH_SIGN_UP_URL)
    public ResponseEntity<UserRegistrationResponse> registration(@RequestBody @Valid UserRegistrationRequest request) {

        UserRegistrationResponse response = userService.registration(request);
        s3FileServiceImpl.createUserDirectory(userService.getIdByUsername(response.username()));
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Авторизует пользователя в системе и создает сеанс"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserAuthorizationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Пользователь уже авторизован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(AUTH_SIGN_IN_URL)
    public ResponseEntity<UserAuthorizationResponse> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                                                   HttpServletRequest servletRequest) {
        UserAuthorizationResponse response = authService.authorization(request, servletRequest);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "Выход из системы",
            description = "Завершает текущий сеанс пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно вышел из системы", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(AUTH_SIGN_OUT_URL)
    public ResponseEntity<Object> logout(HttpServletRequest servletRequest) {
        authService.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }

}