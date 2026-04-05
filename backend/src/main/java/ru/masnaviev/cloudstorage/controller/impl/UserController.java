package ru.masnaviev.cloudstorage.controller.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudstorage.controller.UserApi;
import ru.masnaviev.cloudstorage.dto.response.user.UserMeResponse;
import ru.masnaviev.cloudstorage.model.SecurityUser;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class UserController implements UserApi {

    @Override
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal SecurityUser user) {
        UserMeResponse response = new UserMeResponse(user.getUsername());
        return ResponseEntity.ok().body(response);
    }
}