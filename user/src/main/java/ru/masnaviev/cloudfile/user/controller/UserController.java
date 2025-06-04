package ru.masnaviev.cloudfile.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.user.request.UserCreateRequest;
import ru.masnaviev.cloudfile.user.dto.user.response.UserCreateResponse;

@RestController
@RequiredArgsConstructor
public class UserController {


    @GetMapping("auth/sign-up")
    public ResponseEntity<?> userRegistration(@RequestBody UserCreateRequest request) {


        return new ResponseEntity<>(new UserCreateResponse(), HttpStatusCode.valueOf(201));
    }

}
