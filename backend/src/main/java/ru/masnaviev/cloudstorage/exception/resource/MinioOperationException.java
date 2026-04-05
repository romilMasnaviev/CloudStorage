package ru.masnaviev.cloudstorage.exception.resource;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(Throwable exception) {
        super(exception);
    }
}
