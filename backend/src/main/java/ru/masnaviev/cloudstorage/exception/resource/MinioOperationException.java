package ru.masnaviev.cloudstorage.exception.resource;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
