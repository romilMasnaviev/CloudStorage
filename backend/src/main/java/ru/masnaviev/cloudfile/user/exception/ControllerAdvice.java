package ru.masnaviev.cloudfile.user.exception;

import io.minio.errors.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.masnaviev.cloudfile.user.exception.resource.FileAlreadyExistsException;
import ru.masnaviev.cloudfile.user.exception.resource.PathNotFoundException;
import ru.masnaviev.cloudfile.user.exception.resource.ResourceNotFoundException;
import ru.masnaviev.cloudfile.user.exception.user.UserAlreadyExistsException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.MINIO_EXCEPTION;
import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.RESOURCE_NOT_FOUND;

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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            ServerException.class,
            InsufficientDataException.class,
            IOException.class,
            NoSuchAlgorithmException.class,
            InvalidKeyException.class,
            InvalidResponseException.class,
            XmlParserException.class,
            InternalException.class
    })
    public ResponseEntity<ErrorResponse> minioExceptionHandler(MinioException exception) {
        return new ResponseEntity<>(new ErrorResponse(exception.getMessage()), HttpStatusCode.valueOf(500));
    }
}
