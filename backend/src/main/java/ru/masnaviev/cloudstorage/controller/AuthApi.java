package ru.masnaviev.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.masnaviev.cloudstorage.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudstorage.dto.request.user.UserRegistrationRequest;
import ru.masnaviev.cloudstorage.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudstorage.dto.response.user.UserRegistrationResponse;
import ru.masnaviev.cloudstorage.exception.ErrorResponse;

import static ru.masnaviev.cloudstorage.constants.ApiPath.*;

@Tag(name = "Аутентификация", description = "API для регистрации, авторизации и управления сеансами")
public interface AuthApi {

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
    ResponseEntity<UserRegistrationResponse> registration(@RequestBody @Valid UserRegistrationRequest request);

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
    ResponseEntity<UserAuthorizationResponse> authorization(@RequestBody @Valid UserAuthorizationRequest request,
                                                            HttpServletRequest servletRequest);

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
    ResponseEntity<Object> logout(HttpServletRequest servletRequest);
}
