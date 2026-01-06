package ru.masnaviev.cloudfile.exception.resource;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
