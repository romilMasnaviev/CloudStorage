package ru.masnaviev.cloudfile.exception;

import io.minio.errors.ErrorResponseException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.masnaviev.cloudfile.exception.resource.*;
import ru.masnaviev.cloudfile.exception.user.UserAlreadyExistsException;

import java.util.NoSuchElementException;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.MINIO_EXCEPTION;
import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.RESOURCE_NOT_FOUND;

@RestControllerAdvice
public class ControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(ConstraintViolationException exception) {
        String message = exception.getMessage().substring(exception.getMessage().indexOf(":") + 1);
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatusCode.valueOf(400));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(409));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentialsExceptionHandler(BadCredentialsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(401));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noSuchElementExceptionHandler(NoSuchElementException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(404));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> usernameNotFoundExceptionHandler(UsernameNotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(404));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> fileAlreadyExistsExceptionHandler(FileAlreadyExistsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(409));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> errorResponseExceptionHandler(ErrorResponseException exception) {
        if (exception.errorResponse().code().equals("NoSuchKey")) {
            return new ResponseEntity<>(new ErrorResponse(RESOURCE_NOT_FOUND), HttpStatus.valueOf(404));
        }
        return new ResponseEntity<>(new ErrorResponse(MINIO_EXCEPTION), HttpStatusCode.valueOf(500));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> resourceNotFoundExceptionHandler(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler(PathNotFoundException.class)
    public ResponseEntity<ErrorResponse> pathNotFoundExceptionHandler(PathNotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(400));
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> directoryNotFoundExceptionHandler(DirectoryNotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> fileNotFoundExceptionHandler(FileNotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(404));
    }

    @ExceptionHandler(DirectoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> directoryAlreadyExistsExceptionHandler(DirectoryAlreadyExistsException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(409));
    }

    @ExceptionHandler(PathMustEndWithSlashException.class)
    public ResponseEntity<ErrorResponse> pathMustEndWithSlashExceptionHandler(PathMustEndWithSlashException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(400));
    }

    @ExceptionHandler(MinioOperationException.class)
    public ResponseEntity<ErrorResponse> handleMinioOperationException(MinioOperationException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatusCode.valueOf(500));
    }
}
