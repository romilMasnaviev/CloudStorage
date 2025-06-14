package ru.masnaviev.cloudfile.user.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    @NotBlank(message = "Username must not be empty")
    @Size(min = 8, max = 50, message = "Username length must be between 8 and 50")
    private String username;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, max = 100, message = "Password length must be between 8 and 100")
    private String password;
}
