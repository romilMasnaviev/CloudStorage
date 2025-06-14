package ru.masnaviev.cloudfile.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.masnaviev.cloudfile.user.dto.user.request.UserRegistrationRequest;
import ru.masnaviev.cloudfile.user.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/")
public class AuthController {

    private final AuthService service;

    @PostMapping("sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserRegistrationRequest request) {
        return service.registration(request);
    }

}
//TODO
//	Сначала важно построить функциональный каркас, который обрабатывает запросы.
//	•	Потом улучшать качество данных через валидацию.
//	•	Потом централизовать обработку ошибок — иначе придется дублировать.
//	•	И только потом заботиться о логировании — чтобы логировать реальные, а не фиктивные да