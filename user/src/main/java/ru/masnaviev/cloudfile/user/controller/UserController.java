package ru.masnaviev.cloudfile.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.response.UserMeResponse;

import static ru.masnaviev.cloudfile.user.constatnts.ApiPath.USER_ME_URL;

@RestController
@RequestMapping
class UserController {

    @GetMapping(USER_ME_URL)
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserMeResponse response = new UserMeResponse(userDetails.getUsername());
        return ResponseEntity.ok().body(response);
    }
}
