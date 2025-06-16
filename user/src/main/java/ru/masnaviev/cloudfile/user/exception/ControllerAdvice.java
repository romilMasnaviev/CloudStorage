package ru.masnaviev.cloudfile.user.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.masnaviev.cloudfile.user.dto.user.ErrorResponse;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation error");

        return new ResponseEntity<>(new ErrorResponse(message), exception.getStatusCode());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(409));
    }

    @ExceptionHandler(UserDoesntExistException.class)
    public ResponseEntity<ErrorResponse> userDoesntExistExceptionHandler(UserDoesntExistException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(401));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentialsExceptionHandler(BadCredentialsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(401));
    }
}
