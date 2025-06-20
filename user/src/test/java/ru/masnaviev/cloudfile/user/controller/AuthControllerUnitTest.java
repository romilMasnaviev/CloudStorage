package ru.masnaviev.cloudfile.user.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.masnaviev.cloudfile.user.dto.request.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.request.UserRegistrationRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.masnaviev.cloudfile.user.TestData.*;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.*;

class AuthControllerUnitTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validateRegistrationRequest_whenValidData_thenNoViolations() {
        testRegistrationRequest(USERNAME, PASSWORD, null);
    }

    @Test
    void validateRegistrationRequest_whenEmptyUsername_thenReturnViolation() {
        testRegistrationRequest("", PASSWORD, USERNAME_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateRegistrationRequest_whenUsernameIsNull_thenReturnViolation() {
        testRegistrationRequest(null, PASSWORD, USERNAME_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateRegistrationRequest_whenUsernameTooLong_thenReturnViolation() {
        testRegistrationRequest(TOO_LONG_USERNAME, PASSWORD, USERNAME_LENGTH_BETWEEN_8_50);
    }

    @Test
    void validateRegistrationRequest_whenUsernameTooShort_thenReturnViolation() {
        testRegistrationRequest(TOO_SHORT_USERNAME, PASSWORD, USERNAME_LENGTH_BETWEEN_8_50);
    }


    @Test
    void validateRegistrationRequest_whenEmptyPassword_thenReturnViolation() {
        testRegistrationRequest(USERNAME, "", PASSWORD_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateRegistrationRequest_whenPasswordIsNull_thenReturnViolation() {
        testRegistrationRequest(USERNAME, null, PASSWORD_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateRegistrationRequest_whenPasswordTooLong_thenReturnViolation() {
        testRegistrationRequest(USERNAME, TOO_LONG_PASSWORD, PASSWORD_LENGTH_BETWEEN_8_100);
    }

    @Test
    void validateRegistrationRequest_whenPasswordTooShort_thenReturnViolation() {
        testRegistrationRequest(USERNAME, TOO_SHORT_PASSWORD, PASSWORD_LENGTH_BETWEEN_8_100);
    }

    @Test
    void validateAuthorizationRequest_whenValidData_thenNoViolations() {
        testAuthorizationRequest(USERNAME, PASSWORD, null);
    }

    @Test
    void validateAuthorizationRequest_whenEmptyUsername_thenReturnViolation() {
        testAuthorizationRequest("", PASSWORD, USERNAME_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateAuthorizationRequest_whenUsernameIsNull_thenReturnViolation() {
        testAuthorizationRequest(null, PASSWORD, USERNAME_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateAuthorizationRequest_whenUsernameTooLong_thenReturnViolation() {
        testAuthorizationRequest(TOO_LONG_USERNAME, PASSWORD, USERNAME_LENGTH_BETWEEN_8_50);
    }

    @Test
    void validateAuthorizationRequest_whenUsernameTooShort_thenReturnViolation() {
        testAuthorizationRequest(TOO_SHORT_USERNAME, PASSWORD, USERNAME_LENGTH_BETWEEN_8_50);
    }


    @Test
    void validateAuthorizationRequest_whenEmptyPassword_thenReturnViolation() {
        testAuthorizationRequest(USERNAME, "", PASSWORD_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateAuthorizationRequest_whenPasswordIsNull_thenReturnViolation() {
        testAuthorizationRequest(USERNAME, null, PASSWORD_MUST_NOT_BE_EMPTY);
    }

    @Test
    void validateAuthorizationRequest_whenPasswordTooLong_thenReturnViolation() {
        testAuthorizationRequest(USERNAME, TOO_LONG_PASSWORD, PASSWORD_LENGTH_BETWEEN_8_100);
    }

    @Test
    void validateAuthorizationRequest_whenPasswordTooShort_thenReturnViolation() {
        testAuthorizationRequest(USERNAME, TOO_SHORT_PASSWORD, PASSWORD_LENGTH_BETWEEN_8_100);
    }

    private void testRegistrationRequest(String username, String password, String violationMessage) {
        UserRegistrationRequest request = new UserRegistrationRequest(username, password);
        Set<ConstraintViolation<UserRegistrationRequest>> violations = validator.validate(request);

        if (violationMessage != null) {
            assertTrue(violations
                    .stream()
                    .anyMatch(violation -> violation.getMessage().equals(violationMessage)));
        } else {
            assertTrue(violations.isEmpty());
        }
    }

    private void testAuthorizationRequest(String username, String password, String violationMessage) {
        UserAuthorizationRequest request = new UserAuthorizationRequest(username, password);
        Set<ConstraintViolation<UserAuthorizationRequest>> violations = validator.validate(request);

        if (violationMessage != null) {
            assertTrue(violations
                    .stream()
                    .anyMatch(violation -> violation.getMessage().equals(violationMessage)));
        } else {
            assertTrue(violations.isEmpty());
        }
    }

}