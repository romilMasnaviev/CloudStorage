package ru.masnaviev.cloudfile.controller;

import io.swagger.v3.oas.annotations.Operation;
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
class UserController {

    @Tag(name = "user")
    @Operation(summary = "Get current user info",
            description = "Returns data about the user currently logged in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user info",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "User is not authorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(USER_ME_URL)
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserMeResponse response = new UserMeResponse(userDetails.getUsername());
        return ResponseEntity.ok().body(response);
    }
}
