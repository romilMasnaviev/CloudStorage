package ru.masnaviev.cloudfile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.dto.response.user.UserMeResponse;
import ru.masnaviev.cloudfile.exception.ErrorResponse;

import static ru.masnaviev.cloudfile.constatnts.ApiPath.USER_ME_URL;

@RestController
@RequestMapping
@Tag(name = "Пользователь", description = "API для работы с данными текущего пользователя")
class UserController {

    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает данные о пользователе, который в данный момент авторизован"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация успешно получена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(USER_ME_URL)
    public ResponseEntity<UserMeResponse> me(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        UserMeResponse response = new UserMeResponse(userDetails.getUsername());
        return ResponseEntity.ok().body(response);
    }
}