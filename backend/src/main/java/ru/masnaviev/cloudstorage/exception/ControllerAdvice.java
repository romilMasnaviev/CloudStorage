package ru.masnaviev.cloudstorage.exception;

import io.minio.errors.ErrorResponseException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.masnaviev.cloudstorage.exception.resource.*;
import ru.masnaviev.cloudstorage.exception.user.UserAlreadyExistsException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static ru.masnaviev.cloudstorage.constatnts.ErrorMessages.MINIO_EXCEPTION;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(ConstraintViolationException ex) {
        String message = ex.getMessage().substring(ex.getMessage().indexOf(":") + 1);
        return buildResponseEntity(message, 400);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> userAlreadyExistsExceptionHandler(UserAlreadyExistsException ex) {
        return buildResponseEntity(ex.getMessage(), 409);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentialsExceptionHandler(BadCredentialsException ex) {
        return buildResponseEntity(ex.getMessage(), 401);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> noSuchElementExceptionHandler(NoSuchElementException ex) {
        return buildResponseEntity(ex.getMessage(), 404);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> usernameNotFoundExceptionHandler(UsernameNotFoundException ex) {
        return buildResponseEntity(ex.getMessage(), 404);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> fileAlreadyExistsExceptionHandler(FileAlreadyExistsException ex) {
        return buildResponseEntity(ex.getMessage(), 409);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> errorResponseExceptionHandler(ErrorResponseException ex) {
        if (ex.errorResponse().code().equals("NoSuchKey")) {
            return buildResponseEntity(ex.getMessage(), 404);
        }
        return buildResponseEntity(MINIO_EXCEPTION, 500);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponseEntity(ex.getMessage(), 404);
    }

    @ExceptionHandler(PathNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePathNotFoundException(PathNotFoundException ex) {
        return buildResponseEntity(ex.getMessage(), 400);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDirectoryNotFoundException(DirectoryNotFoundException ex) {
        return buildResponseEntity(ex.getMessage(), 404);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
        return buildResponseEntity(ex.getMessage(), 404);
    }

    @ExceptionHandler(DirectoryAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDirectoryAlreadyExistsException(DirectoryAlreadyExistsException ex) {
        return buildResponseEntity(ex.getMessage(), 409);
    }

    @ExceptionHandler(PathMustEndWithSlashException.class)
    public ResponseEntity<ErrorResponse> handlePathMustEndWithSlashException(PathMustEndWithSlashException ex) {
        return buildResponseEntity(ex.getMessage(), 400);
    }

    @ExceptionHandler(MinioOperationException.class)
    public ResponseEntity<ErrorResponse> handleMinioOperationException(MinioOperationException ex) {
        return buildResponseEntity(ex.getMessage(), 500);

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalArgumentException ex) {
        return buildResponseEntity(ex.getMessage(), 400);
    }

    @ExceptionHandler(ParentDirectoryDeletionException.class)
    public ResponseEntity<ErrorResponse> handleParentDirectoryDeletionException(ParentDirectoryDeletionException ex) {
        return buildResponseEntity(ex.getMessage(), 405);
    }

    @ExceptionHandler(InvalidResourceOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResourceOperationException(InvalidResourceOperationException ex) {
        return buildResponseEntity(ex.getMessage(), 400);
    }

    @ExceptionHandler(InvalidResourceTypeChangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvalidResourceTypeChangeException(InvalidResourceTypeChangeException ex) {
        return buildResponseEntity(ex.getMessage(), 400);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return buildResponseEntity(ex.getMessage(), 413);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildResponseEntity(message, 400);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> globalHandleException(Exception ex) {
        log.error("Неизвестная ошибка", ex);
        return new ResponseEntity<>(new ErrorResponse("Неизвестная ошибка сервера"), HttpStatusCode.valueOf(500));
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(String message, int status) {
        log.warn("Ошибка. Cтатус {}. Сообщение {}", status, message);
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatusCode.valueOf(status));
    }
}
